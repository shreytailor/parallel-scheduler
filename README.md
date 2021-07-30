# The Marauders - Task Scheduler

This is the project repository for Group 7 (*The Marauders*). It consists of the project code, and the documentation produced during the planning and implementation stages.

## Usage Instructions

This task scheduler will take in a directed graph (*.dot* file) as an input. The weighed nodes in this graph will represent tasks with their computation times, while the communication costs between these tasks will be represented by the weighed edges. 

Note that a task here represents a unit of work, which can be scheduled on a processor. The command line arguments which can be used to interact with this program are shown below.

| Arguments (required arguments in italics) | Description |
| --- | --- |
| *INPUT.dot* | A task graph with integer weights in .dot format. |
| *P* | Number of processors to schedule the INPUT graph on. |
| -p N | Use N cores for executing the algorithm in parallel (default is sequential) |
| -v | Visualize the scheduling algorithm. |
| -o OUTPUT | Output file is named OUTPUT (default is INPUT-output.dot) |

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
