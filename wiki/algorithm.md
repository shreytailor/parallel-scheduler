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

