/*
 * Copyright (C) 2013 YROM.NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.acfun.a63.util;

import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;


/**
 * empty implementations of AnimationListener
 * @author Yrom
 *
 */
public abstract class BaseAnimationListener implements AnimationListener {

    /* (non-Javadoc)
     * @see android.view.animation.Animation.AnimationListener#onAnimationEnd(android.view.animation.Animation)
     */
    @Override
    public void onAnimationEnd(Animation animation) {

    }

    /* (non-Javadoc)
     * @see android.view.animation.Animation.AnimationListener#onAnimationRepeat(android.view.animation.Animation)
     */
    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    /* (non-Javadoc)
     * @see android.view.animation.Animation.AnimationListener#onAnimationStart(android.view.animation.Animation)
     */
    @Override
    public void onAnimationStart(Animation animation) {

    }

}
