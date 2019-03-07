package com.adwait.widget.dragcart.utils

import android.animation.ValueAnimator
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v4.view.ViewCompat
import android.view.View
import android.support.v4.view.ViewPropertyAnimatorListener

/**
 * Created by Adwait Abhyankar on 2/20/2019.
 * http://blog.trsquarelab.com/2015/12/creating-custom-animation-in.html
 *  https://android.googlesource.com/platform/frameworks/support/+/c110be5/v7/recyclerview/src/android/support/v7/widget/DefaultItemAnimator.java
 */
class SampleItemAnimator(root: ViewGroup,updateTarget:UpdateListener): SimpleItemAnimator() {

    interface UpdateListener {
        fun onAnimateUpdate(animator:ValueAnimator)
    }

    private val fabX: Float = 300f
    private val fabY: Float = 500f
    private val root: ViewGroup = root
    private val updateTarget = updateTarget
    private lateinit var currentAnimator: ValueAnimator

    private val DEBUG = false
    private val mPendingRemovals = ArrayList<ViewHolder>()
    private val mPendingAdditions = ArrayList<ViewHolder>()
    private val mPendingMoves = ArrayList<MoveInfo>()
    private val mPendingChanges = ArrayList<ChangeInfo>()
    private val mAdditionsList = ArrayList<ArrayList<ViewHolder>>()
    private val mMovesList = ArrayList<ArrayList<MoveInfo>>()
    private val mChangesList = ArrayList<ArrayList<ChangeInfo>>()
    private val mAddAnimations = ArrayList<ViewHolder>()
    private val mMoveAnimations = ArrayList<ViewHolder>()
    private val mRemoveAnimations = ArrayList<ViewHolder>()
    private val mChangeAnimations = ArrayList<ViewHolder>()


    private open class VpaListenerAdapter : ViewPropertyAnimatorListener {
        override fun onAnimationStart(view: View) {}
        override fun onAnimationEnd(view: View) {}
        override fun onAnimationCancel(view: View) {}
    }

    companion object {
        private const val TAG ="ANIMATION"
    }

    override fun animateAdd(p0: RecyclerView.ViewHolder?): Boolean {
        return false
    }

    override fun animateMove(p0: RecyclerView.ViewHolder?, p1: Int, p2: Int, p3: Int, p4: Int): Boolean {
        return false
    }

    override fun animateChange(oldHolder: ViewHolder, newHolder: ViewHolder?,
                               fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        val prevTranslationX = ViewCompat.getTranslationX(oldHolder.itemView)
        val prevTranslationY = ViewCompat.getTranslationY(oldHolder.itemView)
        val prevAlpha = ViewCompat.getAlpha(oldHolder.itemView)
        endAnimation(oldHolder)
        val deltaX = (toX.toFloat() - fromX.toFloat() - prevTranslationX).toInt()
        val deltaY = (toY.toFloat() - fromY.toFloat() - prevTranslationY).toInt()
        // recover prev translation state after ending animation
        ViewCompat.setTranslationX(oldHolder.itemView, prevTranslationX)
        ViewCompat.setTranslationY(oldHolder.itemView, prevTranslationY)
        ViewCompat.setAlpha(oldHolder.itemView, prevAlpha)
        newHolder?.itemView?.let {
            // carry over translation values
            endAnimation(newHolder)
            ViewCompat.setTranslationX(newHolder.itemView, (-deltaX).toFloat())
            ViewCompat.setTranslationY(newHolder.itemView, (-deltaY).toFloat())
            ViewCompat.setAlpha(newHolder.itemView, 0f)
        }
        mPendingChanges.add(ChangeInfo(oldHolder, newHolder!!, fromX, fromY, toX, toY))
        return true
    }

    override fun isRunning(): Boolean {
        return !mPendingAdditions.isEmpty() ||
                !mPendingChanges.isEmpty() ||
                !mPendingMoves.isEmpty() ||
                !mPendingRemovals.isEmpty() ||
                !mMoveAnimations.isEmpty() ||
                !mRemoveAnimations.isEmpty() ||
                !mAddAnimations.isEmpty() ||
                !mChangeAnimations.isEmpty() ||
                !mMovesList.isEmpty() ||
                !mAdditionsList.isEmpty() ||
                !mChangesList.isEmpty()
    }



    private fun endChangeAnimation(infoList: MutableList<ChangeInfo>, item: ViewHolder) {
        for (i in infoList.indices.reversed()) {
            val changeInfo = infoList[i]
            if (endChangeAnimationIfNecessary(changeInfo, item)) {
                if (changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                    infoList.remove(changeInfo)
                }
            }
        }
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder!!)
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder!!)
        }
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo, item: ViewHolder): Boolean {
        var oldItem = false
        when {
            changeInfo.newHolder === item -> changeInfo.newHolder = null
            changeInfo.oldHolder === item -> {
                changeInfo.oldHolder = null
                oldItem = true
            }
            else -> return false
        }
        ViewCompat.setAlpha(item.itemView, 1f)
        ViewCompat.setTranslationX(item.itemView, 0f)
        ViewCompat.setTranslationY(item.itemView, 0f)
        dispatchChangeFinished(item, oldItem)
        return true
    }

    override fun animateRemove(holder: ViewHolder): Boolean {
        endAnimation(holder)
        mPendingRemovals.add(holder)
        return true
    }

    override fun endAnimation(item: ViewHolder) {
        val view = item.itemView
        // this will trigger end callback which should set properties to their target values.
        ViewCompat.animate(view).cancel()
        // TODO if some other animations are chained to end, how do we cancel them as well?
        for (i in mPendingMoves.size - 1 downTo 0) {
            val (holder) = mPendingMoves[i]
            if (holder === item) {
                ViewCompat.setTranslationY(view, 0f)
                ViewCompat.setTranslationX(view, 0f)
                dispatchMoveFinished(item)
                mPendingMoves.removeAt(i)
            }
        }
        endChangeAnimation(mPendingChanges, item)
        if (mPendingRemovals.remove(item)) {
            ViewCompat.setAlpha(view, 1f)
            dispatchRemoveFinished(item)
        }
        if (mPendingAdditions.remove(item)) {
            ViewCompat.setAlpha(view, 1f)
            dispatchAddFinished(item)
        }
        for (i in mChangesList.size - 1 downTo 0) {
            val changes = mChangesList[i]
            endChangeAnimation(changes, item)
            if (changes.isEmpty()) {
                mChangesList.remove(changes)
            }
        }
        for (i in mMovesList.size - 1 downTo 0) {
            val moves = mMovesList[i]
            for (j in moves.size - 1 downTo 0) {
                val (holder) = moves[j]
                if (holder === item) {
                    ViewCompat.setTranslationY(view, 0f)
                    ViewCompat.setTranslationX(view, 0f)
                    dispatchMoveFinished(item)
                    moves.removeAt(j)
                    if (moves.isEmpty()) {
                        mMovesList.remove(moves)
                    }
                    break
                }
            }
        }
        for (i in mAdditionsList.size - 1 downTo 0) {
            val additions = mAdditionsList[i]
            if (additions.remove(item)) {
                ViewCompat.setAlpha(view, 1f)
                dispatchAddFinished(item)
                if (additions.isEmpty()) {
                    mAdditionsList.remove(additions)
                }
            }
        }
        // animations should be ended by the cancel above.
        if (mRemoveAnimations.remove(item) && DEBUG) {
            throw IllegalStateException("after animation is cancelled, item should not be in " + "mRemoveAnimations list")
        }
        if (mAddAnimations.remove(item) && DEBUG) {
            throw IllegalStateException("after animation is cancelled, item should not be in " + "mAddAnimations list")
        }
        if (mChangeAnimations.remove(item) && DEBUG) {
            throw IllegalStateException("after animation is cancelled, item should not be in " + "mChangeAnimations list")
        }
        if (mMoveAnimations.remove(item) && DEBUG) {
            throw IllegalStateException("after animation is cancelled, item should not be in " + "mMoveAnimations list")
        }
        dispatchFinishedWhenDone()
    }

    override fun endAnimations() {
        var count = mPendingMoves.size
        for (i in count - 1 downTo 0) {
            val (holder) = mPendingMoves[i]
            val view = holder.itemView
            ViewCompat.setTranslationY(view, 0f)
            ViewCompat.setTranslationX(view, 0f)
            dispatchMoveFinished(holder)
            mPendingMoves.removeAt(i)
        }
        count = mPendingRemovals.size
        for (i in count - 1 downTo 0) {
            val item = mPendingRemovals[i]
            dispatchRemoveFinished(item)
            mPendingRemovals.removeAt(i)
        }
        count = mPendingAdditions.size
        for (i in count - 1 downTo 0) {
            val item = mPendingAdditions[i]
            val view = item.itemView
            ViewCompat.setAlpha(view, 1f)
            dispatchAddFinished(item)
            mPendingAdditions.removeAt(i)
        }
        count = mPendingChanges.size
        for (i in count - 1 downTo 0) {
            endChangeAnimationIfNecessary(mPendingChanges[i])
        }
        mPendingChanges.clear()
        if (!isRunning) {
            return
        }
        var listCount = mMovesList.size
        for (i in listCount - 1 downTo 0) {
            val moves = mMovesList[i]
            count = moves.size
            for (j in count - 1 downTo 0) {
                val (item) = moves[j]
                val view = item.itemView
                ViewCompat.setTranslationY(view, 0f)
                ViewCompat.setTranslationX(view, 0f)
                dispatchMoveFinished(item)
                moves.removeAt(j)
                if (moves.isEmpty()) {
                    mMovesList.remove(moves)
                }
            }
        }
        listCount = mAdditionsList.size
        for (i in listCount - 1 downTo 0) {
            val additions = mAdditionsList[i]
            count = additions.size
            for (j in count - 1 downTo 0) {
                val item = additions[j]
                val view = item.itemView
                ViewCompat.setAlpha(view, 1f)
                dispatchAddFinished(item)
                additions.removeAt(j)
                if (additions.isEmpty()) {
                    mAdditionsList.remove(additions)
                }
            }
        }
        listCount = mChangesList.size
        for (i in listCount - 1 downTo 0) {
            val changes = mChangesList[i]
            count = changes.size
            for (j in count - 1 downTo 0) {
                endChangeAnimationIfNecessary(changes[j])
                if (changes.isEmpty()) {
                    mChangesList.remove(changes)
                }
            }
        }
        cancelAll(mRemoveAnimations)
        cancelAll(mMoveAnimations)
        cancelAll(mAddAnimations)
        cancelAll(mChangeAnimations)
        dispatchAnimationsFinished()
    }

    fun cancelAll(viewHolders: List<ViewHolder>) {
        for (i in viewHolders.indices.reversed()) {
            ViewCompat.animate(viewHolders[i].itemView).cancel()
        }
    }

    private data class MoveInfo private constructor(var holder: ViewHolder, var fromX: Int, var fromY: Int, var toX: Int, var toY: Int)

    private class ChangeInfo private constructor(var oldHolder: ViewHolder?, var newHolder: ViewHolder?) {
        var fromX: Int = 0
        var fromY: Int = 0
        var toX: Int = 0
        var toY: Int = 0

        constructor(oldHolder: ViewHolder, newHolder: ViewHolder,
                            fromX: Int, fromY: Int, toX: Int, toY: Int) : this(oldHolder, newHolder) {
            this.fromX = fromX
            this.fromY = fromY
            this.toX = toX
            this.toY = toY
        }

        override fun toString(): String {
            return "ChangeInfo{" +
                    "oldHolder=" + oldHolder +
                    ", newHolder=" + newHolder +
                    ", fromX=" + fromX +
                    ", fromY=" + fromY +
                    ", toX=" + toX +
                    ", toY=" + toY +
                    '}'.toString()
        }
    }

    override fun runPendingAnimations() {
        val removalsPending = !mPendingRemovals.isEmpty()
        val movesPending = !mPendingMoves.isEmpty()
        val changesPending = !mPendingChanges.isEmpty()
        val additionsPending = !mPendingAdditions.isEmpty()
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            // nothing to animate
            return
        }
        // First, remove stuff
        for (holder in mPendingRemovals) {
            animateRemoveImpl(holder)
        }
        mPendingRemovals.clear()
        // Next, move stuff
        if (movesPending) {
            val moves = ArrayList<MoveInfo>()
            moves.addAll(mPendingMoves)
            mMovesList.add(moves)
            mPendingMoves.clear()
            val mover = Runnable {
                for ((holder, fromX, fromY, toX, toY) in moves) {
                    animateMoveImpl(holder, fromX, fromY,
                            toX, toY)
                }
                moves.clear()
                mMovesList.remove(moves)
            }
            if (removalsPending) {
                val view = moves[0].holder.itemView
                ViewCompat.postOnAnimationDelayed(view, mover, removeDuration)
            } else {
                mover.run()
            }
        }
        // Next, change stuff, to run in parallel with move animations
        if (changesPending) {
            val changes = ArrayList<ChangeInfo>()
            changes.addAll(mPendingChanges)
            mChangesList.add(changes)
            mPendingChanges.clear()
            val changer = Runnable {
                for (change in changes) {
                    animateChangeImpl(change)
                }
                changes.clear()
                mChangesList.remove(changes)
            }
            if (removalsPending) {
                val holder = changes[0].oldHolder
                ViewCompat.postOnAnimationDelayed(holder!!.itemView, changer, removeDuration)
            } else {
                changer.run()
            }
        }
        // Next, add stuff
        if (additionsPending) {
            val additions = ArrayList<ViewHolder>()
            additions.addAll(mPendingAdditions)
            mAdditionsList.add(additions)
            mPendingAdditions.clear()
            val adder = Runnable {
                for (holder in additions) {
                    animateAddImpl(holder)
                }
                additions.clear()
                mAdditionsList.remove(additions)
            }
            if (removalsPending || movesPending || changesPending) {
                val removeDuration = if (removalsPending) removeDuration else 0
                val moveDuration = if (movesPending) moveDuration else 0
                val changeDuration = if (changesPending) changeDuration else 0
                val totalDelay = removeDuration + Math.max(moveDuration, changeDuration)
                val view = additions[0].itemView
                ViewCompat.postOnAnimationDelayed(view, adder, totalDelay)
            } else {
                adder.run()
            }
        }
    }

    private fun animateRemoveImpl(holder: ViewHolder) {
        val view = holder.itemView
        val animation = ViewCompat.animate(view)
        animation.setDuration(removeDuration)
                .alpha(0f).setListener(object : VpaListenerAdapter() {
                    override fun onAnimationStart(view: View) {
                        dispatchRemoveStarting(holder)
                    }

                    override fun onAnimationEnd(view: View) {
                        animation.setListener(null)
                        ViewCompat.setAlpha(view, 1f)
                        dispatchRemoveFinished(holder)
                        mRemoveAnimations.remove(holder)
                        dispatchFinishedWhenDone()
                    }
                }).start()
        mRemoveAnimations.add(holder)
    }

    private fun animateAddImpl(holder: ViewHolder) {
        val view = holder.itemView
        mAddAnimations.add(holder)
        val animation = ViewCompat.animate(view)
        animation.alpha(1f).setDuration(addDuration).setListener(object : VpaListenerAdapter() {
            override fun onAnimationStart(view: View) {
                dispatchAddStarting(holder)
            }

            override fun onAnimationCancel(view: View) {
                ViewCompat.setAlpha(view, 1f)
            }

            override fun onAnimationEnd(view: View) {
                animation.setListener(null)
                dispatchAddFinished(holder)
                mAddAnimations.remove(holder)
                dispatchFinishedWhenDone()
            }
        }).start()
    }

    private fun animateMoveImpl(holder: ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int) {
        val view = holder.itemView
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        if (deltaX != 0) {
            ViewCompat.animate(view).translationX(0f)
        }
        if (deltaY != 0) {
            ViewCompat.animate(view).translationY(0f)
        }
        // TODO: make EndActions end listeners instead, since end actions aren't called when
        // vpas are canceled (and can't end them. why?)
        // need listener functionality in VPACompat for this. Ick.
        mMoveAnimations.add(holder)
        val animation = ViewCompat.animate(view)
        animation.setDuration(moveDuration).setListener(object : VpaListenerAdapter() {
            override fun onAnimationStart(view: View) {
                dispatchMoveStarting(holder)
            }

            override fun onAnimationCancel(view: View) {
                if (deltaX != 0) {
                    ViewCompat.setTranslationX(view, 0f)
                }
                if (deltaY != 0) {
                    ViewCompat.setTranslationY(view, 0f)
                }
            }

            override fun onAnimationEnd(view: View) {
                animation.setListener(null)
                dispatchMoveFinished(holder)
                mMoveAnimations.remove(holder)
                dispatchFinishedWhenDone()
            }
        }).start()
    }


    private fun animateChangeImpl(changeInfo: ChangeInfo) {
        val holder = changeInfo.oldHolder
        val view = holder?.itemView
        val newHolder = changeInfo.newHolder
        val newView = newHolder?.itemView
        if (view != null) {
            mChangeAnimations.add(changeInfo.oldHolder!!)
            val oldViewAnim = ViewCompat.animate(view).setDuration(
                    changeDuration)
            oldViewAnim.translationX((300 - changeInfo.fromX).toFloat())
            oldViewAnim.translationY((500 - changeInfo.fromY).toFloat())
            oldViewAnim.alpha(0f).setListener(object : VpaListenerAdapter() {
                override fun onAnimationStart(view: View) {
                    dispatchChangeStarting(changeInfo.oldHolder, true)
                }

                override fun onAnimationEnd(view: View) {
                    oldViewAnim.setListener(null)
                    ViewCompat.setAlpha(view, 1f)
                    ViewCompat.setTranslationX(view, 0f)
                    ViewCompat.setTranslationY(view, 0f)
                    dispatchChangeFinished(changeInfo.oldHolder, true)
                    mChangeAnimations.remove(changeInfo.oldHolder!!)
                    dispatchFinishedWhenDone()
                }
            }).start()
        }
        if (newView != null) {
            mChangeAnimations.add(changeInfo.newHolder!!)
            val newViewAnimation = ViewCompat.animate(newView)
            newViewAnimation.translationX(0f).translationY(0f).setDuration(changeDuration).alpha(1f).setListener(object : VpaListenerAdapter() {
                override fun onAnimationStart(view: View) {
                    dispatchChangeStarting(changeInfo.newHolder, false)
                }

                override fun onAnimationEnd(view: View) {
                    newViewAnimation.setListener(null)
                    ViewCompat.setAlpha(newView, 1f)
                    ViewCompat.setTranslationX(newView, 0f)
                    ViewCompat.setTranslationY(newView, 0f)
                    dispatchChangeFinished(changeInfo.newHolder, false)
                    mChangeAnimations.remove(changeInfo.newHolder!!)
                    dispatchFinishedWhenDone()
                }
            }).start()
        }
    }
    /**
     * Check the state of currently pending and running animations. If there are none
     * pending/running, call [.dispatchAnimationsFinished] to notify any
     * listeners.
     */
    private fun dispatchFinishedWhenDone() {
        if (!isRunning) {
            dispatchAnimationsFinished()
        }
    }
}
