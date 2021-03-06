### 19 August
Fix bug with partial expansion and help to implement the parallel scheduler.

### 18 August
Implement equivalent task pruning https://www.sciencedirect.com/science/article/pii/S0305054813002542

### 16 August
Implement equivalent schedule pruning https://www.sciencedirect.com/science/article/pii/S0305054813002542

### 13 August
Implement a method of identifying equivalent tasks which will be used for a pruning technique later on https://link.springer.com/content/pdf/10.1007/s11227-010-0395-1.pdf.

### 12 August
Implement a CLOSED list which stores fully expanded states. Now states will only be added to the OPEN list if they cannot be found in the CLOSED list.

### 11 August
Implement partial expansion https://link.springer.com/content/pdf/10.1007/s11227-010-0395-1.pdf

Improve heuristic function using ideas from https://link.springer.com/content/pdf/10.1007/s11227-010-0395-1.pdf

### 4 August
Fixed a bug which caused a lot of states to not be checked.

Reduced the memory usage of the scheduling algorithm by replacing HashMaps with byte and int arrays. Now each task has an ID which is used to index into these arrays.

### 3 August
Improve the heuristic of the scheduling algorithm. Strategies used include: 

Adding node priorities (i.e. nodes with higher priorities are scheduled first, priority in this case is referring to the bottom level of the node).*Only used for the feasible schedule algorithm

Initially generating a feasible schedule using a greedy approach, the makespan of this schedule is then used as an upper bound to prune schedules that cannot be optimal. Tasks are scheduled according to their node priorities.

The origin of these ideas are from the following paper: https://www.researchgate.net/publication/222302496_On_multiprocessor_task_scheduling_using_efficient_state_space_search_approaches

### 2 August
Implemented the cost function for the scheduling algorithm. The cost function is calculated by getting the cost to reach the current state plus the estimated cost of reaching the goal state from the current state. This estimate is calculated by finding the static level of the current node. A more elaborate explanation is found here: https://www.researchgate.net/publication/222302496_On_multiprocessor_task_scheduling_using_efficient_state_space_search_approaches

### 30 July
Changed DOTParser to be static along with some further refactoring.

### 29 July
Implemented a method for generating a DOT file from a schedule.

### 28 July
Implemented a method to read DOT files into a graph through the use of a digraph-parser library (https://github.com/paypal/digraph-parser).

Implemented a brute force algorithm (breadth first search).

### 27 July
Created the classes required for the scheduling algorithm. Helped finish the Plan by writing explanations for the WBS.

### 26 July
Finished WBS, Network diagram and Gantt chart.

### 23 July
Researched and compared A* and branch and bound algorithms.

### 22 July
Discussed project requirements and started on the Work Breakdown Structure and Network Diagram.
