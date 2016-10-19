/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.android.contacts.common.list;


import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * An animation which can rotate on Y axis, the start and end degree
 * can be specified, currently used in BladeView pop up window animation.
 */
public class PromptRotateAnimation extends Animation {
    private static final String TAG = "PopupRotateAnimation";
    
    private float mFromDegree;
    private float mToDegree;
    private float mPivotX;
    private Camera mCamera;
    
    /**
     * Simple constructor, need to specify all parameters.
     * 
     * @param fromDegree The rotate start degree.
     * @param toDegree The rotate end degree.
     * @param pivotX The X axis pivot of the rotate.
     */
    public PromptRotateAnimation(float fromDegree, float toDegree, float pivotX) {
        mFromDegree = fromDegree;
        mToDegree = toDegree;
        mPivotX = pivotX;
        mCamera = new Camera();
    }
    
    /**
     * Set rotate interval.
     * 
     * @param fromDegree The rotate start degree.
     * @param toDegree The rotate end degree.
     */
    public void setDegreeInterval(final float fromDegree, final float toDegree) {
        mFromDegree = fromDegree;
        mToDegree = toDegree;
    }
    
    /**
     * Set X axis pivot.
     * 
     * @param pivot The X axis pivot of the rotate.
     */
    public void setPivot(final float pivot) {
        mPivotX = pivot;
    }
    
    @Override
    protected void applyTransformation(final float interpolatedTime, 
            final Transformation t) {
        final float fromDegree = mFromDegree;
        float degree = fromDegree + ((mToDegree - fromDegree) * interpolatedTime);
        
        final Camera camera = mCamera;
        final Matrix matrix = t.getMatrix();
        camera.save();
        
        /* Rotate subject on Y axis. */
        camera.rotateY(-degree);
        camera.getMatrix(matrix);
        camera.restore();

        /* Translate the pivot. */
        matrix.preTranslate(-mPivotX, 0);
        matrix.postTranslate(mPivotX, 0);
    }
}
