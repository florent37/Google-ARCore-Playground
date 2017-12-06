# Google ARCore Playground

# Prequel

https://developers.google.com/ar/develop/

```
ARCore provides SDKs for many of the most popular development environments.
These SDKs provide native APIs for all of the essential AR features like motion tracking, environmental understanding, and light estimation. With these capabilities you can build entirely new AR experiences or enhance existing apps with AR features.
```

# Running on your device

```
Google's ARCore developer preview for Android is awesome.
Unfortunately, Android phone like OnePlus is not on the supported list,
and apps built with ARCore exit at start on my device.

However, its hardware actually can run ARCore!
```

Using https://github.com/tomthecarrot/arcore-for-all, arCore can run on unsuported devices (only for test purposes)

# What the sample does

[![planes](https://raw.githubusercontent.com/florent37/Google-ARCore-Playground/master/medias/plane.jpg)](https://github.com/florent37/Google-ARCore-Playground)

[![lines](https://raw.githubusercontent.com/florent37/Google-ARCore-Playground/master/medias/with_lines.jpg)](https://github.com/florent37/Google-ARCore-Playground)

# Understand ARCore

ARCore main goal is to understand his environment,
to acomplish this, following camera's video it try to find landmarks in space,
then combine them with all device's hardware sensors to track motion and find our exactly position in this environment.

ARCore is capable of detecting planes (eg: ground / surface of a table), and attach anchor point on this environment.
It means if you add an object at an exact point of your environment, for example adding a bottle (by OpenGL) in the middle of you table,
and you move your phone to make it become out of screen, if you get back to the table, you will be able to see again at the same place your bottle.

ARCore, by the `Session` singleton class, provides the description of the environment.

At each instant (eg: when we want to draw), we ask ARCore for a `Frame`

A Frame is the explanation of the environment at the instant X

A `Frame` contains :

- The list of Points he detects on the camera `.getPointCloud()`
- An estimation of the light intensity `.getLightEstimate()`
- The current view matrix `.getViewMatrix(matrix, ...)`

And the `Session` contains :
- The list of detected Planes `.getAllPlanes()`
- The list of all saved anchor points `.getAllAnchors()`
- The current projection matrix `.getProjectionMatrix(matrix, ...)`

Then, an `Anchor` offer
- a position in space named `Pose`

# Matrix, Reloaded

## Projection

As developer, the principal main objects we will need is `Pose`

Because a Pose contains, for a plane, an object of even a point  :
- The `translation` (tx, ty, tz) we need to apply on it
- The `rotation` (rx, ry, rz) we need to apply on it

To move your object at the pose's exact position you just have to multiply your model's projection matrix by the Pose's matrix

```
OBJECT_3D.proj_matrix = OBJECT_3D.proj_matrix * OBJECT_3D.anchor.pose.proj_matrix
```

In java we will use the method `anchor.getPose().toMatrix(float[]) to obtain the anchor projection matrix

## Camera

And, do not fo forget to update your camera matrix,
to ensure your objects to be drawn & visible at the perfect pose in space

`arcoreFrame.getViewMatrix(float[], 0);`