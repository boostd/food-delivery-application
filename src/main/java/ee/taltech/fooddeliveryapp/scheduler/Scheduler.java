package ee.taltech.fooddeliveryapp.scheduler;

import jakarta.annotation.PostConstruct;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.concurrent.ScheduledFuture;

public  class Scheduler implements Runnable {

    @SuppressWarnings("rawtypes")
    private ScheduledFuture scheduledFuture;
    private TaskScheduler taskScheduler;
    private final ImportWeatherTask task = new ImportWeatherTask();

    /**
     * This method kills the previous scheduler if it exists and creates a new scheduler with given cron expression.
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
        this.reSchedule("15 * * * *");
    }
}
