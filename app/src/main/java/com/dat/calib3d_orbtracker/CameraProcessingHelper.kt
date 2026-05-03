package com.dat.calib3d_orbtracker

import org.opencv.calib3d.Calib3d
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble

object CameraProcessingHelper {

    fun applyUndistort(src: Mat, f: Double, cx: Double, cy: Double): Mat {
        val dst = Mat()

        // Define Camera Matrix K
        val cameraMatrix = Mat(3, 3, CvType.CV_64F)
        cameraMatrix.put(0, 0, f, 0.0, cx, 0.0, f, cy, 0.0, 0.0, 1.0)

        // Define Distortion Coefficients (k1, k2, p1, p2, k3)
        // Values typical for wide-angle smartphone lenses (Barrel Distortion)
        val distCoeffs = MatOfDouble(-0.2, 0.1, 0.0, 0.0, 0.0)

        // Perform correction
        Calib3d.undistort(src, dst, cameraMatrix, distCoeffs)

        return dst
    }
}
