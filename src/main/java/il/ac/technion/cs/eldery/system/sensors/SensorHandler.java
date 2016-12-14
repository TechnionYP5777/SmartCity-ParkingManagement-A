package il.ac.technion.cs.eldery.system.sensors;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

import il.ac.technion.cs.eldery.networking.messages.Message;
import il.ac.technion.cs.eldery.networking.messages.MessageFactory;
import il.ac.technion.cs.eldery.networking.messages.RegisterMessage;
import il.ac.technion.cs.eldery.networking.messages.UpdateMessage;

public class SensorHandler implements Runnable {
    @SuppressWarnings("rawtypes") private Map<String, SensorInfo> sensors = new HashMap<>();

    @SuppressWarnings("rawtypes") public Map<String, SensorInfo> getSensors() {
        return sensors;
    }

    @Override public void run() {
        try (DatagramSocket socket = new DatagramSocket(100)) {
            byte[] buffer = new byte[2048];

            for (DatagramPacket packet = new DatagramPacket(buffer, buffer.length);;) {
                socket.receive(packet);
                String json = new String(buffer, 0, packet.getLength());

                Message message = MessageFactory.create(json);

                if (message == null)
                    continue;

                switch (message.getType()) {
                    case REGISTRATION:
                        handleRegisterMessage(packet, (RegisterMessage) message);
                        break;
                    case UPDATE:
                        handleUpdateMessage(packet, (UpdateMessage) message);
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRegisterMessage(DatagramPacket packet, RegisterMessage ¢) {
        sensors.put(¢.getSensor().getId(), new SensorInfo<>(¢.getSensor().getId(), 100));
        
        // TODO: Sharon, send back a success message
    }

    @SuppressWarnings("unused") private void handleUpdateMessage(DatagramPacket packet, UpdateMessage m) {
        // TODO: Sharon, implement
    }
}
