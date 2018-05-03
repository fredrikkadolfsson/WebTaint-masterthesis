import os
import subprocess
import time

runtTestsXTimes = 1


def main():
    print("Starting Benchmarking Script\n\r")
    clean = ["Clean", 'java']

    # ##### Dynamic Taint Tracking Tools #####
    # Dynamic Taint Tracker
    dtpXboot = "-Xbootclasspath/p:/home/fredrik/Documents/Omegapoint/Benchmarking/Phosphor/jre-instr"
    dtpAgent = "-javaagent:/home/fredrik/Documents/Omegapoint/masterthesis-fredrik/code/dtp/build/libs/dtp-agent-1.0-SNAPSHOT.jar"
    dynamicTaintTracker = ["Dynamic Taint Tracker", 'java', dtpXboot, dtpAgent]

    # Dynamic Security Taint Propagation
    stpXboot = "-Xbootclasspath/p:/home/fredrik/Documents/Omegapoint/Benchmarking/Dynamic Taint Trackers/security_taint_propagation/security_taint_extension/target/tainted-rt-1.8.jar"
    stpAgent = "-javaagent:/home/fredrik/Documents/Omegapoint/Benchmarking/Dynamic Taint Trackers/security_taint_propagation/aspectjweaver-1.9.1.jar"
    securityTaintPropagation = [
        "Dynamic Security Taint Propagation", 'java',   stpXboot, stpAgent]

    # Phosphor
    phosJava = "/home/fredrik/Documents/Omegapoint/Benchmarking/Dynamic Taint Trackers/phosphor-master/Phosphor/target/jre-inst/bin/java"
    phosXboot = "-Xbootclasspath/a:/home/fredrik/Documents/Omegapoint/Benchmarking/Dynamic Taint Trackers/phosphor-master/Phosphor/target/Phosphor-0.0.4-SNAPSHOT.jar"
    phosAgent = "-javaagent:/home/fredrik/Documents/Omegapoint/Benchmarking/Dynamic Taint Trackers/phosphor-master/Phosphor/target/Phosphor-0.0.4-SNAPSHOT.jar"
    phosphor = ["Phosphor", phosJava, phosXboot, phosAgent]

    # ##### Test Suits #####
    # DeCapo
    daCapo = [
        '-jar', "/home/fredrik/Documents/Omegapoint/Benchmarking/Test Suites/dacapo-9.12-MR1-bach.jar"]
    # daCapoPrograms = ["avrora", "eclipse", "fop",  "h2", "jython", "luindex", "lusearch", "pmd", "sunflow", "tomcat", "tradebeans", "tradesoap", "xalan"]
    daCapoPrograms = ["fop", "jython"]

    print("##### ADDED OVERHEAD #####")
    for daCapoProgram in daCapoPrograms:
        print("\n\r\t# DaCapo " + daCapoProgram)

        for trackingTools in [clean, dynamicTaintTracker, securityTaintPropagation, phosphor]:
            [text, *rest] = trackingTools

            daCapoExec = [text] + \
                rest + daCapo + [daCapoProgram]
            measureTime(*daCapoExec)

    print("\n\rEnd Benchmarking Script")


def current_milli_time(): return int(round(time.time() * 1000))


def measureTime(text, *params):
    FNULL = open(os.devnull, 'w')

    averageTime = 0
    fastestTime = 0
    slowestTime = 0
    for x in range(1, runtTestsXTimes + 1):
        print("", end="\r")
        print("\t\tLoading (", x, "/", runtTestsXTimes, ")", end='', flush=True)

        start_time = int(round(time.time() * 1000))
        retcode = subprocess.call(
            params, stdout=FNULL, stderr=subprocess.STDOUT)
        time_diff = current_milli_time() - start_time

        averageTime += time_diff / runtTestsXTimes
        if fastestTime < time_diff:
            fastestTime = time_diff
        if slowestTime > time_diff or slowestTime == 0:
            slowestTime = time_diff

    print("", end="\r")
    print("\t\t" + text, "OK" if retcode == 0 else "ERROR",
          "                                                   ")
    if retcode == 0:
        print("\t\t\t Average", round(averageTime), "(ms)")
        print("\t\t\t Fastest", round(fastestTime), "(ms)")
        print("\t\t\t Slowest", round(slowestTime), "(ms)")


if __name__ == "__main__":
    main()
