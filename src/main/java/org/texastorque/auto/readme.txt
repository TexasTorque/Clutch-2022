AUTO
====
All autonomous components are based off of the 2022 
Torque Auto Framework, located in TorqueLib at 
"org.texastorque.torquelib.auto.*".


AutoManager
===========
A singleton class that extends TorqueAutoManager.
Controls the sendable chooser that sets the current
autonomous sequence and executes them.

commands
========
A package of command classes, extending TorqueCommand,
for use by sequences.

sequences
=========
A package of sequence classes, extending TorqueSequence,
for use by the auto manager.
