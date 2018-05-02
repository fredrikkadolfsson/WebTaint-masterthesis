import os
import subprocess
import time

runtTestsXTimes = 1


def main():
    print("Starting Benchmarking Script\n\r")

    # ##### Dynamic Taint Tracking Tools #####
    # Dynamic Taint Tracker
    dtpXboot = "-Xbootclasspath/p:/home/fredrik/Documents/Omegapoint/Benchmarking/Phosphor/jre-instr"
    dtpAgent = "-javaagent:/home/fredrik/Documents/Omegapoint/masterthesis-fredrik/code/dtp/build/libs/dtp-agent-1.0-SNAPSHOT.jar"

    # Phosphor
    phosXboot = "-Xbootclasspath/p:/home/fredrik/Documents/Omegapoint/Benchmarking/Dynamic Taint Trackers/phosphor-master/Phosphor/target/Phosphor-0.0.4-SNAPSHOT.jar"
    phosAgent = "-javaagent:/home/fredrik/Documents/Omegapoint/Benchmarking/Dynamic Taint Trackers/phosphor-master/Phosphor/target/Phosphor-0.0.4-SNAPSHOT.jar"

    # Dynamic Security Taint Propagation
    stpXboot = "-Xbootclasspath/p:/home/fredrik/Documents/Omegapoint/Benchmarking/Dynamic Taint Trackers/security_taint_propagation/security_taint_extension/target/tainted-rt-1.8.jar"
    stpAgent = "-javaagent:/home/fredrik/Documents/Omegapoint/Benchmarking/Dynamic Taint Trackers/security_taint_propagation/aspectjweaver-1.9.1.jar"

    # ##### Test Suits #####
    # DeCapo
    daCapo = "/home/fredrik/Documents/Omegapoint/Benchmarking/Test Suites/dacapo-9.12-MR1-bach.jar"

    cleanDeCapoAvrora = ["Clean DeCapo Avrora",
                         'java', '-jar', daCapo, "avrora"]
    dTPDeCapoAvrora = ["DTP DeCapo Avrora", 'java',
                       dtpXboot, dtpAgent, '-jar', daCapo, "avrora"]
    phosDeCapoAvrora = ["Phosphor DeCapo Avrora", 'java',
                        phosXboot, phosAgent, '-jar', daCapo, "avrora"]
    stpDeCapoAvrora = ["Dynamic Security Taint Propagation", 'java',
                       stpXboot, stpAgent, '-jar', daCapo, "avrora"]

    # measureTime(*cleanDeCapoAvrora)
    # measureTime(*dTPDeCapoAvrora)
    measureTime(*phosDeCapoAvrora)  # Not Working
    # measureTime(*stpDeCapoAvrora)

    print("\n\rEnd Benchmarking Script")


def current_milli_time(): return int(round(time.time() * 1000))


def measureTime(text, *params):
    FNULL = open(os.devnull, 'w')

    averageTime = 0
    fastestTime = 0
    slowestTime = 0
    for x in range(1, runtTestsXTimes + 1):
        print("", end="\r")
        print("Loading (", x, "/", runtTestsXTimes, ")", end='', flush=True)

        start_time = int(round(time.time() * 1000))
        retcode = subprocess.call(
            params)
        # params, stdout=FNULL, stderr=subprocess.STDOUT)
        time_diff = current_milli_time() - start_time

        averageTime += time_diff / runtTestsXTimes
        if fastestTime < time_diff:
            fastestTime = time_diff
        if slowestTime > time_diff or slowestTime == 0:
            slowestTime = time_diff

    print("", end="\r")
    print(text, "OK" if retcode == 0 else "ERROR")
    print("\t Average time", round(averageTime), "(ms)")
    print("\t Fastest time", round(fastestTime), "(ms)")
    print("\t Slowest time", round(slowestTime), "(ms)")


if __name__ == "__main__":
    main()
