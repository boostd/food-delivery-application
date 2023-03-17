package ee.taltech.fooddeliveryapp.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.concurrent.ScheduledFuture;

import static org.mockito.Mockito.*;

class SchedulerTest {

    @Mock
    private ImportWeatherTask importWeatherTask;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private ScheduledFuture<?> scheduledFuture;

    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduler = new Scheduler(importWeatherTask);
        scheduler.setTaskScheduler(taskScheduler);
        scheduler.setScheduledFuture(scheduledFuture);
    }

    /**
     * Tests the re-scheduling functionality by verifying the scheduler is rescheduled with a new cron expression.
     */
    @Test
    void testReSchedule() {
        String cronExpression = "0 0 * * * *";

        scheduler.reSchedule(cronExpression);

        verify(taskScheduler).schedule(scheduler, new CronTrigger(cronExpression));
        verify(scheduledFuture).cancel(true);
    }

    /**
     * Tests the run() method by verifying the updateWeather() method of ImportWeatherTask is called.
     */
    @Test
    public void testRun() {
        // Act
        scheduler.run();

        // Assert
        verify(importWeatherTask).updateWeather();
    }

    /**
     * Tests the initialization of the scheduler, making sure updateWeather() is called
     * and the scheduler is scheduled using the default cron expression.
     */
    @Test
    public void testInitializeScheduler() {
        // Act
        scheduler.initializeScheduler();

        // Assert
        verify(importWeatherTask).updateWeather();
        verify(taskScheduler).schedule(any(Runnable.class), eq(new CronTrigger("0 15 * * * *")));
    }

}
