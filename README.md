# Process Manager

This is example implementation of process manager. It supports actions as:

- adding new process
- killing the process by its id
- killing processes with given priority
- killing all processes
- listing all processes

There are three strategies of handling overflow (when manager is full, but you try to add new process):

- skip [default] - new process will be skipped
- remove oldest - the oldest process will be removed and new will be added
- remove oldest with the lowest priority 

## Assumptions

Code was written with such assumptions:

- it uses kind of `CAS` instead of locking, so potentially order of new processes may be not guaranteed
- simplicity is most important, so this is not optimized for performance
- there won't be added new strategies (this is not util library), so strategies are hardcoded (otherwise they may be proved as a parameter on building phase)
- creating process is cheap (process is created before it gets added, so newly created process may be dismissed)

