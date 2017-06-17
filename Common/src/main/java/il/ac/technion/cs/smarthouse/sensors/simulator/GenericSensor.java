package il.ac.technion.cs.smarthouse.sensors.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.ac.technion.cs.smarthouse.sensors.InstructionHandler;
import il.ac.technion.cs.smarthouse.sensors.InteractiveSensor;

/**
 * @author Elia Traore
 * @since Jun 17, 2017
 */
class GenericSensor {	
	private static Logger log = LoggerFactory.getLogger(GenericSensor.class);

	@SuppressWarnings("rawtypes")
	private class MsgStreamerThread extends Thread {
		private Map<String, List> ranges;
		private Boolean keepStreaming = true;

		public MsgStreamerThread(final Map<String, List> ranges){
			this.ranges = ranges;
		}

		@Override
		public void interrupt() {
			keepStreaming = false;
			log.debug("streamer was interruped. Stopping.");
			super.interrupt();
		}

		@Override
		public void run() {
			if(!legalRanges())
				return;
            while(keepStreaming){
            	Map<String, Object> data = new HashMap<>();
                ranges.keySet().forEach(path -> data.put(path, random(path)));
                sendMessage(data);
            }
		}
		
		@SuppressWarnings("unchecked")
		private boolean legalRanges() {
			return paths.get(PathType.MESSAGE).keySet().stream().map(path -> {
						Class c = paths.get(PathType.MESSAGE).get(path);
						List vals = ranges.get(path);
						Boolean $ = Integer.class.isAssignableFrom(c) || Double.class.isAssignableFrom(c) ? vals.size() == 2 : true;
						vals.stream().map(o -> c.isAssignableFrom(o.getClass()))
									.reduce((x,y)-> x && y)
									.ifPresent(b -> $ &= b);
						return $;
					}).reduce((x,y)-> x && y).orElse(false);
		}

		private Object random(String path) {
			Class c = paths.get(PathType.MESSAGE).get(path);
			List vals = ranges.get(path);
            if(Integer.class.isAssignableFrom(c))
            	return ThreadLocalRandom.current().nextInt((Integer)vals.get(0), (Integer)vals.get(1));
            if(Double.class.isAssignableFrom(c))
                return ThreadLocalRandom.current().nextDouble((Double)vals.get(0), (Double)vals.get(1));
            else
            	return vals.get(ThreadLocalRandom.current().nextInt(vals.size()));
		}
	}
	
    private Map<PathType, List<Consumer<String>>> loggers = new HashMap<>();
    private Map<PathType, Map<String,Class>> paths = new HashMap<>();
    private Boolean interactive, connected = false;
    
    private Map<String, List> LastReceivedRanges;
    
    private InteractiveSensor sensor;
    
    private MsgStreamerThread msgStreamer;
    
    GenericSensor(){}
    
    private void connectIfNeeded(){
    	if(connected)
    		return;
    	while(!sensor.register());
    	if(interactive){
    		while(!sensor.registerInstructions());
    		sensor.pollInstructions(TimeUnit.SECONDS.toMillis(30));
    	}
    	connected = true;
    }
   
    //------------------------ setters --------------------------------------
    void addPath(PathType type, String path, Class pathClass){
        if(!paths.containsKey(type))
            paths.put(type, new HashMap<>());
        paths.get(type).put(path, pathClass);
        interactive |= PathType.INSTRUCTIONS.equals(type);
    }
    
    void addRange(String path, @SuppressWarnings("rawtypes") List values){
    	if(LastReceivedRanges == null)
    		LastReceivedRanges = new HashMap<>();
    	LastReceivedRanges.put(path, values);
    }

    void addLogger(PathType t, Consumer<String> logger){
    	if(!loggers.containsKey(t))
    		loggers.put(t, new ArrayList<>());
        loggers.get(t).add(logger);
    }
    
    void setSensor(InteractiveSensor s){
    	sensor = s;
    }

    //------------------------ getters --------------------------------------
    List<String> getPaths(PathType type){
    	return Optional.ofNullable(paths.get(type))
    					.map(ps -> new ArrayList<>(ps.keySet()))
    					.orElse(new ArrayList<>());
    }
    
    InteractiveSensor getSensor(){
    	return sensor;
    }

    //------------------------ logging --------------------------------------
    void logInstruction(String path, String inst){
    	loggers.get(PathType.INSTRUCTIONS).forEach(logger -> logger.accept("Received instruction:\n\t"+inst+"\nOn path:\n\t"+path));
    }
    
    void logMsgSent(Map<String,String> data){
    	final List<String> $ = new ArrayList<>();
    	$.add("Sending following msg:\n");
    	data.keySet().forEach(path -> $.add("path:"+path+"\tvalue:"+data.get(path)));
    	
    	$.stream().reduce((x,y)-> x+y).ifPresent(
    			formatedData -> loggers.get(PathType.INSTRUCTIONS).forEach(logger -> logger.accept(formatedData)));
    }
    
    //------------------------ public getters -------------------------------
    public String getCommname() {
        return sensor.getCommname();
    }

    
    public String getId() {
        return sensor.getId();
    }

    
    public String getAlias() {
        return sensor.getAlias();
    }

    
    public List<String> getObservationSendingPaths() {
        return sensor.getObservationSendingPaths();
    }

    
    public List<String> getInstructionRecievingPaths() {
        return sensor.getInstructionRecievingPaths();
    }
    
  //------------------------ Data sending methods -------------------------
    
    /** this method is blocking in the first time it called
     * */
    public void sendMessage(Map<String, Object> data){//todo: change to package level?
    	connectIfNeeded();
        
    	if(!paths.containsKey(PathType.MESSAGE))
            return;
        Map<String, String> d = new HashMap<>();
        data.keySet().forEach(k -> d.put(k, data.get(k)+""));
        sensor.updateSystem(d);
        logMsgSent(d);
    }
    
    
    /** the list object is interperated by the path (key type)
     * if the type is string or boolean, the list contains all the legal values
     * if the type is double or integer, the list contains (low,high) so that low <= legal values < high
     * */
    void streamMessages(final Map<String, List> ranges){
        if(!paths.containsKey(PathType.MESSAGE))
            return;
        
        LastReceivedRanges = ranges;

        if(msgStreamer != null){
        	msgStreamer.interrupt();
        	try {
				msgStreamer.join();
			} catch (InterruptedException e) {
				log.warn("Simulator was interrupted will waiting for a msg streamer");
			}
        }
        
        msgStreamer = new MsgStreamerThread(ranges);
        msgStreamer.start();
    }
    
    
    /** streams with the ranges given throught the builder
     * */
    void streamMessages(){
    	if(LastReceivedRanges == null)
    		return;
    	streamMessages(LastReceivedRanges);
    }
    
}