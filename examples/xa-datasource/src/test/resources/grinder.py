from net.grinder.script.Grinder import grinder
from net.grinder.script import Test
from org.superbiz.perf import DBTestPerf

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
