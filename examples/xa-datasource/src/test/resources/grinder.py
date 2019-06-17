# Hello World
#
# A minimal script that tests The Grinder logging facility.
#
# This script shows the recommended style for scripts, with a
# TestRunner class. The script is executed just once by each worker
# process and defines the TestRunner class. The Grinder creates an
# instance of TestRunner for each worker thread, and repeatedly calls
# the instance for each run of that thread.

from net.grinder.script.Grinder import grinder
from net.grinder.script import Test
from org.superbiz.perf import DBTestPerf

# A shorter alias for the grinder.logger.output() method.
log = grinder.logger.output

tests = {
    "get" : Test(1, "get")
    }

loadBean = DBTestPerf("http://localhost:9080/")
get = tests["get"].wrap(loadBean)

# A TestRunner instance is created for each thread. It can be used to
# store thread-specific data.
class TestRunner:

    # This method is called for every run.
    def __call__(self):
        get.get()
