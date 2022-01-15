We need to refactor this base package. Oct 17 2021.

I dont belive that a minor change like unabstracting
a class justifies the creation of a new class. 

[ ] Merge TorqueInput.java and TorqueInputModule.java
[ ] Merge TorqueFeedback.java and TorqueFeedbackModule.java

We DO NOT NEED TorqueClass. That is completely 
unnecessary and rediculus over abstraction. 

[X] Remove TorqueClass.java

The subsystem base class should be moved here, why?
It has never been added to TorqueLib due to containing 
an instance reference to Input and Feedback. This is 
unnecessary, and is more explicit to use:
  Input.getInstance().doSomething();
rather than
  Input input = Input.getInstance();
  input.doSomething();
Since this is being done more often (like on the original 
2021 swerve drive testing repo) base classes that held 
these references can be relocated. 

[X] Add standard Subsystem.java as TorqueSubsystem.java

Note, only one instance of the class needs to be in an 
instance reference variable to prevent it from being
discarded by the garbage collector. This is held in the
instance variable in Robot.java.

This also extends to Auto baseclasses, but we that is a
topic for another day.
