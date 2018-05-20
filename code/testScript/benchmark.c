#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <fcntl.h>
#include <time.h>

int main(int argc, char *argv[])
{
  pid_t childpid;
  struct rusage usage;
  struct timespec start, end;
  long mem_usage = 0, mem_average = 0, mem_min = 0, mem_max = 0;
  u_int64_t time_usage = 0, time_average = 0, time_min = 0,time_max = 0;
  int j, k, i, fd[2];
  int val = 0;

  int runtTestsXTimes = 10;
  const char *file = "java";

  // ##### Dynamic Taint Tracking Tools #####
  // # Dynamic Taint Tracker
  const char *dtpXboot = "-Xbootclasspath/p:/home/fredrik/Documents/Omegapoint/masterthesis-fredrik/code/dtp/build/libs/dtp-rt-1.0-SNAPSHOT.jar";
  const char *dtpAgent = "-javaagent:/home/fredrik/Documents/Omegapoint/masterthesis-fredrik/code/dtp/build/libs/dtp-agent-1.0-SNAPSHOT.jar";

  // ##### Test Suits #####
  // DeCapo
  const char *daCapo = "/home/fredrik/Documents/Omegapoint/Benchmarking/TestSuites/dacapo-9.12-MR1-bach.jar";
  const char *daCapoPrograms[] = {"avrora", "batik", "eclipse", "fop",  "h2", "jython", "luindex", "lusearch", "pmd", "sunflow", "tomcat", "tradebeans", "tradesoap", "xalan"};

  for (j = 0; j < sizeof(daCapoPrograms) / sizeof(daCapoPrograms[0]); j++) {
    printf("# DaCapo %s\n", daCapoPrograms[j]);
    printf("\tClean\n");
    
    for (i = 0 ; i < runtTestsXTimes; i++) {
      printf("\t\tLoading (%d / %d)", i + 1, runtTestsXTimes);
      fflush( stdout );
      // create pipe descriptors
      pipe(fd);

      childpid = fork();
      if(childpid != 0)  // parent
      {
        close(fd[1]);
        // read the data (blocking operation)
        read(fd[0], &mem_usage, sizeof(mem_usage));
        read(fd[0], &time_usage, sizeof(time_usage));

        mem_average += mem_usage / runtTestsXTimes;
        time_average += time_usage / runtTestsXTimes;

        if(mem_usage > mem_max) mem_max = mem_usage;
        if(mem_usage < mem_min || mem_min == 0) mem_min = mem_usage;

        if(time_usage > time_max) time_max = time_usage;
        if(time_usage < time_min || time_min == 0) time_min = time_usage;

        // close the read-descriptor
        close(fd[0]);
      }
      else  // child
      {    
        close(fd[0]); // writing only, no need for read-descriptor
        
        int returnStatus;  
        
        clock_gettime(CLOCK_MONOTONIC_RAW, &start);
        childpid = fork();
        if(childpid != 0)  // parent
        {
          waitpid(childpid, &returnStatus, 0);
          clock_gettime(CLOCK_MONOTONIC_RAW, &end);
          time_usage = (end.tv_sec - start.tv_sec) * 1000000 + (end.tv_nsec - start.tv_nsec) / 1000;
        }
        else  // child
        {
          int fd2 = open("/dev/null", O_WRONLY);
          dup2(fd2, 1);
          dup2(fd2, 2);
          close(fd2);
          execl("/usr/bin/java", "java", "-jar", daCapo, daCapoPrograms[j], NULL);
        }

        getrusage(RUSAGE_CHILDREN, &usage);
        mem_usage = usage.ru_maxrss;

        write(fd[1], &mem_usage, sizeof(mem_usage)); // send the value on the write-descriptor
        write(fd[1], &time_usage, sizeof(time_usage)); // send the value on the write-descriptor

        close(fd[1]); // close the write descriptor
        return mem_usage;
      }
    }

    printf("\r\t\tTime (microseconds)\n");
    printf("\t\t\tAverage: %ld\n", time_average);
    printf("\t\t\tMin: %ld\n", time_min);
    printf("\t\t\tMax: %ld\n", time_max);

    printf("\t\tMemory (kilobytes)\n");
    printf("\t\t\tAverage: %ld\n", mem_average);
    printf("\t\t\tMin: %ld\n", mem_min);
    printf("\t\t\tMax: %ld\n", mem_max);

    time_average = time_min = time_max = 0;
    mem_average = mem_min = mem_max = 0;

    printf("\tDynamic Taint Tracking\n");
    
    for (i = 0 ; i < runtTestsXTimes; i++) {
      printf("\t\tLoading (%d / %d)", i + 1, runtTestsXTimes);
      fflush( stdout );
      // create pipe descriptors
      pipe(fd);

      childpid = fork();
      if(childpid != 0)  // parent
      {
        close(fd[1]);
        // read the data (blocking operation)
        read(fd[0], &mem_usage, sizeof(mem_usage));
        read(fd[0], &time_usage, sizeof(time_usage));

        mem_average += mem_usage / runtTestsXTimes;
        time_average += time_usage / runtTestsXTimes;

        if(mem_usage > mem_max) mem_max = mem_usage;
        if(mem_usage < mem_min || mem_min == 0) mem_min = mem_usage;

        if(time_usage > time_max) time_max = time_usage;
        if(time_usage < time_min || time_min == 0) time_min = time_usage;

        // close the read-descriptor
        close(fd[0]);
      }
      else  // child
      {    
        close(fd[0]); // writing only, no need for read-descriptor
        
        int returnStatus;  
        
        clock_gettime(CLOCK_MONOTONIC_RAW, &start);
        childpid = fork();
        if(childpid != 0)  // parent
        {
          waitpid(childpid, &returnStatus, 0);
          clock_gettime(CLOCK_MONOTONIC_RAW, &end);
          time_usage = (end.tv_sec - start.tv_sec) * 1000000 + (end.tv_nsec - start.tv_nsec) / 1000;
        }
        else  // child
        {
          int fd2 = open("/dev/null", O_WRONLY);
          dup2(fd2, 1);
          dup2(fd2, 2);
          close(fd2);
          execl("/usr/bin/java", "java", dtpXboot, dtpAgent, "-jar", daCapo, daCapoPrograms[j], NULL);
        }

        getrusage(RUSAGE_CHILDREN, &usage);
        mem_usage = usage.ru_maxrss;

        write(fd[1], &mem_usage, sizeof(mem_usage)); // send the value on the write-descriptor
        write(fd[1], &time_usage, sizeof(time_usage)); // send the value on the write-descriptor

        close(fd[1]); // close the write descriptor
        return mem_usage;
      }
    }

    printf("\r\t\tTime (microseconds)\n");
    printf("\t\t\tAverage: %ld\n", time_average);
    printf("\t\t\tMin: %ld\n", time_min);
    printf("\t\t\tMax: %ld\n", time_max);

    printf("\t\tMemory (kilobytes)\n");
    printf("\t\t\tAverage: %ld\n", mem_average);
    printf("\t\t\tMin: %ld\n", mem_min);
    printf("\t\t\tMax: %ld\n\n", mem_max);

    time_average = time_min = time_max = 0;
    mem_average = mem_min = mem_max = 0;
  }

}
