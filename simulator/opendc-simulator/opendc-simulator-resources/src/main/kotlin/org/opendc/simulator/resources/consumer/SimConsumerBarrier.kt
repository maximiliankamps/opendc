/*
 * Copyright (c) 2021 AtLarge Research
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.opendc.simulator.resources.consumer

/**
 * The [SimConsumerBarrier] is a barrier that allows consumers to wait for a select number of other consumers to
 * complete, before proceeding its operation.
 */
public class SimConsumerBarrier(public val parties: Int) {
    private var counter = 0

    /**
     * Enter the barrier and determine whether the caller is the last to reach the barrier.
     *
     * @return `true` if the caller is the last to reach the barrier, `false` otherwise.
     */
    public fun enter(): Boolean {
        val last = ++counter == parties
        if (last) {
            counter = 0
            return true
        }
        return false
    }

    /**
     * Reset the barrier.
     */
    public fun reset() {
        counter = 0
    }
}
