# Python
Place create Python files that have relation to TorqueLib components here.

## Modules
### ballseer.py
![Example in SmartDashboard and OutlineViewer](https://github.com/TexasTorque/TorqueLib/blob/master/python/ballseer.png?raw=true)
BallSeer works together with [TorqueBallSeer.java](https://github.com/TexasTorque/TorqueLib/blob/master/component/TorqueBallSeer.java) to send the location of the center of the most prominent yellow ball. It was created during the 2021 season. 

Network Table:
| Entry Name | Type | Description |
| --- | --- | --- |
| frame_width | double | Width of the camera frame |
| frame_height | double | Height of the camera frame |
| target_location | double[2] | Center_x and center_y |
| reset | boolean | Signal to reset BallSeer's target_location |
