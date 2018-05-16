import os
import subprocess
import time
import resource

runtTestsXTimes = 5


def main():
    print("Starting Benchmarking Script\n\r")
    clean = ["Clean", 'java']

    # ##### Dynamic Taint Tracking Tools #####
    # Dynamic Taint Tracker
    dtpXboot = "-Xbootclasspath/p:/home/fredrik/Documents/Omegapoint/masterthesis-fredrik/code/dtp/build/libs/dtp-rt-1.0-SNAPSHOT.jar"
    dtpAgent = "-javaagent:/home/fredrik/Documents/Omegapoint/masterthesis-fredrik/code/dtp/build/libs/dtp-agent-1.0-SNAPSHOT.jar"
    dynamicTaintTracker = ["Dynamic Taint Tracker", 'java', dtpXboot, dtpAgent]
    # -Xbootclasspath/p:/home/fredrik/Documents/Omegapoint/masterthesis-fredrik/code/dtp/build/libs/dtp-rt-1.0-SNAPSHOT.jar -javaagent:/home/fredrik/Documents/Omegapoint/masterthesis-fredrik/code/dtp/build/libs/dtp-agent-1.0-SNAPSHOT.jar

    # Dynamic Security Taint Propagation DONT WORK!!!
    stpXboot = "-Xbootclasspath/p:/home/fredrik/Documents/Omegapoint/Benchmarking/DynamicTaintTrackers/security_taint_propagation/security_taint_extension/target/tainted-rt-1.8.jar"
    stpAgent = "-javaagent:/home/fredrik/Documents/Omegapoint/Benchmarking/DynamicTaintTrackers/security_taint_propagation/aspectjweaver-1.9.1.jar"
    securityTaintPropagation = [
        "Dynamic Security Taint Propagation", 'java',   stpXboot, stpAgent]
    # -Xbootclasspath/p:/home/fredrik/Documents/Omegapoint/Benchmarking/Dynamic\ Taint\ Trackers/security_taint_propagation/security_taint_extension/target/tainted-rt-1.8.jar -javaagent:/home/fredrik/Documents/Omegapoint/Benchmarking/Dynamic\ Taint\ Trackers/security_taint_propagation/aspectjweaver-1.9.1.jar

    # Phosphor
    phosJava = "/home/fredrik/Documents/Omegapoint/Benchmarking/DynamicTaintTrackers/phosphor-master/Phosphor/target/jre-inst/bin/java"
    phosXboot = "-Xbootclasspath/a:/home/fredrik/Documents/Omegapoint/Benchmarking/DynamicTaintTrackers/phosphor-master/Phosphor/target/Phosphor-0.0.4-SNAPSHOT.jar"
    phosAgent = "-javaagent:/home/fredrik/Documents/Omegapoint/Benchmarking/DynamicTaintTrackers/phosphor-master/Phosphor/target/Phosphor-0.0.4-SNAPSHOT.jar"
    phosphor = ["Phosphor", phosJava, phosXboot, phosAgent]

    # ##### Test Suits #####
    # DeCapo
    daCapo = [
        '-jar', "/home/fredrik/Documents/Omegapoint/Benchmarking/TestSuites/dacapo-9.12-MR1-bach.jar"]
    # daCapoPrograms = ["avrora"]
    daCapoPrograms = ["avrora", "eclipse", "fop",  "h2", "jython", "luindex",
                      "lusearch", "pmd", "sunflow", "tomcat", "tradebeans", "tradesoap", "xalan"]

    print("##### ADDED OVERHEAD #####")
    for daCapoProgram in daCapoPrograms:
        print("\n\r\t# DaCapo " + daCapoProgram)

        # for trackingTools in [clean]:
        for trackingTools in [clean, dynamicTaintTracker]:
            [text, *rest] = trackingTools

            daCapoExec = [text] + \
                rest + daCapo + [daCapoProgram]
            print(daCapoExec)
            measureTime(*daCapoExec)

    print("\n\rEnd Benchmarking Script")


def current_milli_time(): return int(round(time.time() * 1000))


def measureTime(text, *params):
    FNULL = open(os.devnull, 'w')

    averageTime = 0
    fastestTime = 0
    slowestTime = 0
    averageMem = 0
    fastestMem = 0
    slowestMem = 0
    for x in range(1, runtTestsXTimes + 1):
        print("", end="\r")
        print("\t\tLoading (", x, "/", runtTestsXTimes, ")", end='', flush=True)

        start_time = int(round(time.time() * 1000))

        p = subprocess.Popen(params, stdout=FNULL, stderr=subprocess.STDOUT)
        p.wait()
        time_diff = current_milli_time() - start_time

        retcode = p.returncode

        memUsageKilobytes = resource.getrusage(
            resource.RUSAGE_CHILDREN).ru_maxrss

        averageTime += time_diff / runtTestsXTimes
        averageMem += memUsageKilobytes / runtTestsXTimes

        if fastestTime < time_diff:
            fastestTime = time_diff
        if slowestTime > time_diff or slowestTime == 0:
            slowestTime = time_diff

        if fastestMem < memUsageKilobytes:
            fastestMem = memUsageKilobytes
        if slowestMem > memUsageKilobytes or slowestMem == 0:
            slowestMem = memUsageKilobytes

    print("", end="\r")
    print("\t\t" + text, "OK" if retcode == 0 else "ERROR",
          "                                                   ")
    if retcode == 0:
        print("\t\t\t Average", round(averageTime),
              "(ms) ", round(averageMem), "(kilobytes)")
        print("\t\t\t Fastest", round(fastestTime),
              "(ms) ", round(fastestMem), "(kilobytes)")
        print("\t\t\t Slowest", round(slowestTime),
              "(ms) ", round(slowestMem), "(kilobytes)")


if __name__ == "__main__":
    main()
