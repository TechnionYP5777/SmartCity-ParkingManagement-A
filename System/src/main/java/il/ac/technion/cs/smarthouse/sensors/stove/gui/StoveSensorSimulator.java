package il.ac.technion.cs.smarthouse.sensors.stove.gui;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import il.ac.technion.cs.smarthouse.sensors.simulator.GenericSensor;
import il.ac.technion.cs.smarthouse.sensors.simulator.SensorBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This class simulates a temperature sensor for a stove.
 * 
 * @author Sharon
 * @since 9.12.16
 */
public class StoveSensorSimulator extends Application {
	public static void main(final String[] args) {
		launch(args);
	}

	GenericSensor stoveSensor;
	private void startSensor(Consumer<String> logger) {
		final String onPath = "stove" + "." + "is_on";
		final String temperPath = "stove" + "." + "temperature";
		stoveSensor =new SensorBuilder()
		        .setCommname("iStoves")
		        .setAlias("Roy's awesome stove")
				.addInfoSendingPath(onPath, Boolean.class)
				.addInfoSendingPath(temperPath, Integer.class)
				.addStreamingRange(onPath, Arrays.asList(true, false))
				.addStreamingRange(temperPath, Arrays.asList(0, 140))
				.setStreamInterval(1000L)//TimeUnit.SECONDS.toMillis(1))
				.addInfoSendingLogger(logger)
				.build();
		stoveSensor.startStreaming();
	}

	private Scene setFromSensorSimulator() {
		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(20));
		box.setSpacing(20.0);

		startSensor(msg -> Platform.runLater(() -> {
			box.getChildren().add(new Label(msg));
			if (box.getChildren().size() > 50)
				box.getChildren().remove(0);
		}));

		ScrollPane pane = new ScrollPane();
		pane.setPrefSize(500, 500);
		pane.setContent(box);
		VBox parentBox = new VBox();
		parentBox.getChildren().add(new Label("Im a place holder! :D heres a basic console:\n"));
		parentBox.getChildren().add(pane);
		return new Scene(parentBox);
	}

	@Override
	public void start(final Stage s) throws Exception {
		// s.setScene(setFromController());
		s.setScene(setFromSensorSimulator());
		s.setTitle("Stove Sensor Simulator");
		s.setOnCloseRequest(e -> Optional.ofNullable(stoveSensor).ifPresent(sensor -> sensor.stopStreaming()));
		s.show();
	}
}
