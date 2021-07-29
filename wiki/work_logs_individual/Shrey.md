# Shrey's Work Log

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