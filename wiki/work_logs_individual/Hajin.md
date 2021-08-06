### 5 Aug
- Explored different options of profilers to detect what objects are created. [VisualVM](https://visualvm.github.io) comes as part of JDK8, so it was fit for initial choice.
- Execution file can be found from /Library/Java/JavaVirtualMachines/jdk1.8.0_261.jdk/Contents/Home/bin/jvisualvm. After running the program on IntelliJ, the process (com.team7.EntryPoint) on the left pane. 

### 4 Aug
- Refactored com.team7.Scheduler into more modular methods to increase each method's testability and reduce chains of dependencies. Created 2 additional util classes (algoutils.Preprocess, ScheduleCalculator). Undertook this refactoring while constantly checking against tests we have (this saved me because sometimes things would unexpectedly break. yay to TDD!).
- com.team7.Scheduler#findOptimalSchedule() should look more closer to the pseudocode. Also it's more readable and maintainable this way.
- Added new test suite (factory) called SchedulerTestLectureExample, which makes it easy to test solely lecture examples. Since it tests both optimality (expected vs actual finish time) and the validity, it was necessary to separate this from test cases that only check constraints not optimality.

### 2 Aug
- Researched parallelisation and pruning techniques from [this paper](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.62.8293&rep=rep1&type=pdf)

### 30 Jul
- Created a dot file that reflects two graphs from lecture slides
- Communicated solutions to the team about some of the issues I encountered while getting Shrey's newly built JavaFX application to run. 
1. need fxml dependency
2. need to wrap plugins tag with build tag 
3. mvn clean install -U helps if you want to clean install dependencies 
4. here's an example of program argument for Entrypoint run configuration: task-scheduler/src/dot-tests/lectureExample.DOT 2 -v
5. NotImplementedException is only supported up to Java 6

### 28 Jul
- Reviewed Oscar's implementation of brute force algorithm
- Refactored the A star algorithm into several methods for testability and readability. Added comments to assist other group members' understanding.
- Created JUnit5 tests for testing the validity of the schedule produced by the above brute force algorithm. There are two constraints to be accounted for - Processor and Precedence constraints. Only Processor constraints have been tested so far. 
- Implemented [TestFactory](https://www.baeldung.com/junit5-dynamic-tests) that creates test cases dynamically for each dot files Simon and Shrey have created.
- Suggested fix for a recursive call between classes in execution of hashCode/toString/Equals that caused Stack Overflow Exception.

### 27 Jul
- Discussed the algorithm with team members, referring to several papers
- Wrote pseudocode for the A star algorithm

### 26 Jul
- Consolidated WBS, Network diagram
- Finished Gantt chart with the team
- Wrote [meeting minutes](../minutes/26-Jul.md)
- Structured packages for the project
- Created Config class with getters and setters that will store various configuration states (whether the program has visualisation, 
- Added test cases to ParserTest class to test command line input parsing. This is to check that the program processes arguments as intended, and whether it handles erratic cases (eg: whether default values are assigned to configuration if no argument is provided, whether exception is raised for inappropriate patterns of command input) 

### 22 Jul

- Discussed about the project requirement with the team member and collaborated with others in the making of WBS and Network Diagram
- Organised wiki structure (minutes, work log), and wrote [meeting minutes](../minutes/22-Jul.md) 
- A research into a suitable algorithm is in the process, where I would initially want to understand [Oliver Sinnen's paper](https://www.sciencedirect.com/science/article/pii/S0305054813002542?fbclid=IwAR34tKob8V73ri4qL_I9PzJsxBY6pRtJBb9p9BU3K9NPu17-C4UdpLRiWNg)
  - One of this this is A* algorithm
  - [This](https://www.youtube.com/watch?v=ySN5Wnu88nE) helped me with intuition
  - [This](https://www.youtube.com/watch?v=-L-WgKMFuhE) helped me with formulating pseudocode

