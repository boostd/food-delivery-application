package ee.taltech.fooddeliveryapp.scheduler;

import jakarta.annotation.PostConstruct;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledFuture;

@Getter
@Setter
@Component
public class Scheduler implements Runnable {

    @SuppressWarnings("rawtypes")
    private ScheduledFuture scheduledFuture;
    private TaskScheduler taskScheduler;
    private final ImportWeatherTask task;

    @Autowired
    Scheduler(ImportWeatherTask task) {
        this.task = task;
    }


    /**
     * This method kills the previous scheduler if it exists and creates a new scheduler with given cron expression.
     * In other words this method is used to reschedule the timing of the request for weather data.
     *
     * @param cronExpressionStr New cron expression to create schedule with
     */
    public void reSchedule(String cronExpressionStr) {
        if (taskScheduler == null) {
            this.taskScheduler = new ConcurrentTaskScheduler();
        }
        if (this.scheduledFuture != null) {
            this.scheduledFuture.cancel(true);
        }
        this.scheduledFuture = this.taskScheduler.schedule(this, new CronTrigger(cronExpressionStr));
    }

    /**
     * Gets an XML file from
     * <a href="https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php">the weather service.</a>
     * Then parses the file and writes the weather data from the Tallinn-Harku, Tartu-Tõravere and Pärnu stations
     * into the database.
     */
    @Override
    public void run() {
        task.updateWeather();
    }

    /**
     * Initializes the scheduler with the default timing for CronJob (HH:15:00)
     */
    @PostConstruct
    public void initializeScheduler() {
        this.run();
        this.reSchedule("0 15 * * * *");
    }
}
