### 4 August
Fixed a bug which caused a lot of states to not be checked.

Reduced the memory usage of the scheduling algorithm by replacing HashMaps with byte and int arrays.

### 3 August
Improve the heuristic of the scheduling algorithm. Strategies used include: 

Adding node priorities (i.e. nodes with higher priorities are scheduled first, priority in this case is referring to the sum of the bottom and top levels of the node).

Initially generating a feasible schedule using a greedy approach, the makespan of this schedule is then used as an upper bound to prune schedules that cannot be optimal.

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
