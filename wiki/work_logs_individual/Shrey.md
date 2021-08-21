# Shrey's Work Log

## 21/08/2021
- Stress tested the whole implementation using different input graphs (*small* and *large*) to find any bugs, if any exist. The test was done using two computers, with and without visualization to check if the graphical user interface is making an impact.
- **Refactoring:** Added lots of JavaDocs to the codebase, so its easy for readers to determine what the code is doing.

---

## 20/08/2021
- In the afternoon, I took part in a Zoom peer-programming session with Josh and Hajin, in order to solve the issues we were having regarding updating the visualization (and its communication with the algorithm). We managed to fix the issue together.
- Implemented some final features for the visualization.
  - Stopping the graph from updating once the algorithm execution has finished. This involved tweaking the mechanisms of `ScheduleUpdater` and `TimeProvider` APIs, and reflect that change over to the `SchedulerScreenController`.
  - Updating the statistics as the algorithm runs, after deciding with the team that we want to show the number of *open states* and *closed states*. 
- Documented most of the Graphical User Interface classes, and made sure the code was as descriptive as possible for the readers.

---

## 19/08/2021
- Continued to work on the things from yesterday, however had a few issues which must be communicated with the team in order to resolve it.

---

## 18/08/2021
- Added onto the implementation of the existing `ScheduleUpdater` class to make it more efficient to show the visualization, and refactored some of the code that deals with linking the algorithm, middleware, and visualization together.

---

## 17/08/2021
- Implemented some functionality of the `ScheduleUpdater` class, which acts as a middleware between the algorithm implementation and the Graphical User Interface.
- This class tries to poll the data from the `Scheduler` class, and pass that onto the visualization every `x` milliseconds, which then triggers the graph update.

---

## 10/08/2021
- Currently, the visualization is only useful to check the final schedule once the algorithm has completed its execution. However, for the next iteration of the application, the GUI would be required to show the schedule as the algorithm runs (so we can see the *best schedule yet*.)
- Before starting the implementation, it was important to research on how this part of the application would be architectured so I used the following resources to learn more about JavaFX. 
  - https://edencoding.com/force-refresh-scene/
  - https://www.developer.com/java/data/understanding-java-observable-and-javafx-observable/

---

## 08/08/2021
- Reviewing the report for Milestone 1 and the implementation itself, and checking where all the client requirements have been met.
- Refactoring the `visualisation` package by renaming some classes and writing extensive JavaDoc for all the classes and comments.

---

## 07/08/2021
- Since most of the implementation was completed for Milestone 1 (except for some documentation, commenting, and refactoring which wouldn't change the functionality), I had worked on the report for this milestone.
  - Wrote up content for the following sections
    - *Major Work Tasks*
    - *Individual Contributions* - Reflecting on meeting notes to see who has worked on which tasks.
    - *Development Processes*
    - *Tools and Technology* - Also explaining why these choices were the ideal for our group environment.

---

## 05/08/2021
- Had a discussion with Josh about the next steps for the visualization development, so we both made a plan for the next tasks.
- We also sat together and slightly changed the current high-fidelity prototype in Figma, by including accent colours and custom fonts to the layout.

---

## 04/08/2021
- Completely refactored  directories storing files for the visualization. 
  - This process included creating new packages and renaming classes, because a lot of the current classes were named really similarly (either had prefix of `Visualisation...` or `Schedule...`).
  - Removed files which are not required and used anymore.
- Added a new feature to the schedule representation.
  - It enables the viewers to see which bar (in the *gantt chart*) belongs to which task from the `.dot` file.

---

## 03/08/2021
- For the initial GUI of the application, I started out by implementing the graphs for CPU and RAM utilization. These were chosen as the first step (before working on the dynamically changing graph), as they do not depend on the event-emitting system which is yet to be created by us.
- The `OperatingSystemMXBean` was used to get important information about the computer, from Java. This bean also provides more useful data such as the time since the process started, which is going to be useful later to develop the other parts of the interface.
- The following resources were used to help me implement these graphs. These are listed as references for the future work.
  - [Everything you need to know about JavaFX Charts](https://edencoding.com/javafx-charts/)
  - [How to force refresh the Scene in JavaFX (and 3 reasons you don’t have to)](https://edencoding.com/force-refresh-scene/)
  - [Definitive Guide To JavaFX Events](https://edencoding.com/javafx-events/)

---

## 31/07/2021
- Learnt more about the heuristic function and various (pruning) techniques used to reduce the search complexity for the optimal solution.
  - [Paper Source](https://onlinelibrary.wiley.com/doi/full/10.1002/cpe.5898)
- Watched a couple of videos on the algorithm as well.
  - [Video Source](https://www.youtube.com/watch?v=wJ9RyRBkgPo&t=111s)

---

## 30/07/2021

- Editing the Maven configuration file (`pom.xml`) to include the dependencies for JavaFX, because we are starting to scaffold a simple view for algorithm visualization.
- Created a custom JavaFX Gantt Chart component which will display a simple view of the optimal schedule that is created. This will be displayed after the algorithm finishes its execution.
  - **Note:** This is going to be changed heavily and currently only created so that the algorithm developers have a view of what the final schedule looks like.
- With the created visualization, I researched a little about how to setup an event (or listener) based system so that we can run the JavaFX application while the algorithm is running (which would be more helpful).
  - [Reference Documentation](https://www3.cs.stonybrook.edu/~pfodor/courses/CSE114/L15-EventDrivenProgrammingInJavaFX.pdf)

---

## 29/07/2021

- Sat down with some teammates this morning, and completed the low-fi prototype based on the feedback. The final version can be seen [here](./../interface-prototype.md).
- Integrated the current algorithm implementation to the command line utility, in the `Entrypoint` class so that everything works from scratch.
- Researched slightly on the Java libraries we can use to show the different graph visualizations; the dynamically changing best schedule and the static input graph.
  - [GraphStream](https://graphstream-project.org/) - this library supports *dynamic* graph so there is a possibility that it has its own event-emitting system.
  - [Graphviz](https://graphviz.org/) - this library creates static representations of the graphs which means we might have to implement our own events through the algorithm execution, if we were to use this.

---

## 28/07/2021

- Created some changes to the existing low-fi prototype.
- Changed the `DOTParser` slightly, and added getters/setters for its properties.
- Refactored the repository structure by repackaging the classes.
- Created the `hashCode()`, `equals()` and `toString()` methods for some of the classes.
  - This was later refactored because of a circular dependencies between the two classes.
- After getting the .dot input files from Simon, I had created a unit testing suite for the `DOTParser` class (consisting of around four to five test cases).

---

## 27/07/2021

- For the Planning report, I wrote the description of our Network Chart and helped finalize it for submission.
- Assisted the group in creating a generic class structure, required to represent the graph states later on.
- Implemented custom exception handling in the command-line parsing logic, by creating the `CommandLineException`. This is useful for providing custom exception details to users when required, and compliments the business logic.
- Added the Git Conventions and Project Plan sections to the README file.
- Finalized the low-fidelity prototype.
- Submitted the planning report.

---

## 26/07/2021

- Helped the group finalize the Work Breakdown Structure, and Network Diagram.
- Started working on the low-fidelity prototype for the GUI.
- Designed an architecture for the command-line input and argument parsing.
- Implemented the `Config` class using the `commons-cli` library.
  - It would read the string argument, and produce a `Config` instance from that.
  - The creation of the class was supported by test-driven development, using the created test suite.

---

## 22/07/2021

- Collaborate with the other group members in creating the Work Breakdown Structure and Network Diagram.
- As discussed in the meeting today, I performed some initial research on the algorithms (*A** and *Branch and Bound*).
  - https://www.youtube.com/watch?v=PzEWHH2v3TE&ab_channel=Education4u
- Organize the README. for the repository homepage, so that it includes information about the team, its group members, and a link to our project wiki.

---