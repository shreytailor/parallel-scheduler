```pseudocode
Function OptimalSchedule:
	PriorityQueue Open
	Open.Insert(initialState)
	Set Closed

	While Open.isNotEmpty():
		state <- Open.Pop()	
		if state.isLast():
			return state
			
		dependents <- state.Expand()
		foreach dependent in dependents:
			dependent.f = dependent.h + dependent.g
			if dependent not in Open and Closed:
				Open.Insert(dependent)
		Closed.Insert(task)
```

**Cost function:** 

The cost function is calculated by getting the cost to reach the current state plus the estimated cost of reaching the goal state from the current state. This estimate is calculated by finding the static level of the current node. 

A more elaborate explanation is found here: https://www.researchgate.net/publication/222302496_On_multiprocessor_task_scheduling_using_efficient_state_space_search_approaches

**Heuristics used:** 

Adding node priorities (i.e. nodes with higher priorities are scheduled first, priority in this case is referring to the sum of the bottom and top levels of the node).

Initially generating a feasible schedule using a greedy approach, the makespan of this schedule is then used as an upper bound to prune schedules that cannot be optimal.

These ideas are from the following paper: https://www.researchgate.net/publication/222302496_On_multiprocessor_task_scheduling_using_efficient_state_space_search_approaches
