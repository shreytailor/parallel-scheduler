# The Marauders - Task Scheduler

This is the project repository for Group 7 (*The Marauders*). It consists of the project code, and the documentation produced during the planning and implementation stages.

## Usage Instructions

## Environment

The `scheduler.jar` file located in the project root has been tested on Java 11 (in the FlexIT machine), and it was confirmed to be working as expected by the development team.

### Arguments/Inputs

This task scheduler will take in a directed graph (*.dot* file) as an input. Tasks and their computation times are represented in the graph as weighted nodes, while the communication costs between these tasks will be represented by the weighted edges. Dot files should be placed in the same folder as the jar file.

The command line arguments which can be used to interact with this program are shown below.

| Arguments (required arguments in italics) | Description |
| --- | --- |
| *INPUT.dot* | A task graph with integer weights in .dot format. |
| *P* | Number of processors to schedule the INPUT graph on. |
| -p N | Use N cores for executing the algorithm in parallel (default is sequential) |
| -v | Visualize the scheduling algorithm. |
| -o OUTPUT | Output file is named OUTPUT (default is INPUT-output.dot) |

Example usages: 
- ```java -jar scheduler.jar Nodes_11_OutTree.dot 2 -v -p 4```
- ```java -jar scheduler.jar "./task-scheduler/src/crawled-dot-tests/Fork_Join_Nodes_10_CCR_0.10_WeightType_Random#5_Homogeneous-4.dot" 8 -v```


## Team

| Name | UPI | GitHub Username |
| --- | --- | --- |
| Simon Cheng | sche987 | [simoncheng987](https://github.com/simoncheng987) |
| Hajin Kim | hkim532 | [hajineats](https://github.com/hajineats) |
| Shrey Tailor | stai259 | [shreytailor](https://github.com/shreytailor) |
| Oscar Li | oli356 | [oscarli00](https://github.com/oscarli00) |
| Josh Lim | jlim322 | [JoshXLim](https://github.com/JoshXLim) |

## Wiki

The project wiki contains information about the meeting minutes, documentation, and other plans creating during the various stages. It can be accessed from [here](wiki/index.md).
