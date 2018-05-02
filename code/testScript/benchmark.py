import os
import subprocess
import time

runtTestsXTimes = 1


def main():
    print("Starting Benchmarking Script\n\r")

    # ##### Dynamic Taint Tracking Tools #####
    # Dynamic Taint Tracker
    dtpXboot = "-Xbootclasspath/p:/home/fredrik/Documents/Omegapoint/masterthesis-fredrik/code/dtp/build/libs/dtp-rt-1.0-SNAPSHOT.jar"
    dtpAgent = "-javaagent:/home/fredrik/Documents/Omegapoint/masterthesis-fredrik/code/dtp/build/libs/dtp-agent-1.0-SNAPSHOT.jar"

    # ##### Test Suits #####
    # DeCapo
    daCapo = "/home/fredrik/Documents/Omegapoint/Benchmarking/Test Suites/dacapo-9.12-MR1-bach.jar"

    cleanDeCapoAvrora = ["Clean DeCapo Avrora",
                         'java', '-jar', daCapo, "avrora"]
    dTPDeCapoAvrora = ["DTP DeCapo Avrora", 'java',
                       dtpXboot, dtpAgent, '-jar', daCapo, "avrora"]

    measureTime(*cleanDeCapoAvrora)
    # measureTime(*dTPDeCapoAvrora)

    print("\n\rEnd Benchmarking Script")


def current_milli_time(): return int(round(time.time() * 1000))


def measureTime(text, *params):
    FNULL = open(os.devnull, 'w')

    averageTime = 0
    for x in range(1, runtTestsXTimes + 1):
        print("", end="\r")
        print("Loading (", x, "/", runtTestsXTimes, ")", end='', flush=True)

        start_time = int(round(time.time() * 1000))
        retcode = subprocess.call(
            params, stdout=FNULL, stderr=subprocess.STDOUT)
        time_diff = current_milli_time() - start_time

        averageTime = time_diff / runtTestsXTimes

    print("", end="\r")
    print("OK" if retcode == 0 else "ERROR ", text, averageTime)


if __name__ == "__main__":
    main()
