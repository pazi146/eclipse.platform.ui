package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.EventObject;

/**
 * Interface for objects that support elements with a checked state.
 *
 * @see ICheckStateListener
 * @see CheckStateChangedEvent
 */
public interface ICheckable {
/**
 * Adds a listener for changes to the checked state of elements
 * in this viewer.
 * Has no effect if an identical listener is already registered.
 *
 * @param listener a check state listener
 */
public void addCheckStateListener(ICheckStateListener listener);
/**
 * Returns the checked state of the given element.
 *
 * @param element the element
 * @return <code>true</code> if the element is checked,
 *   and <code>false</code> if not checked
 */
public boolean getChecked(Object element);
/**
 * Removes the given check state listener from this viewer.
 * Has no effect if an identical listener is not registered.
 *
 * @param listener a check state listener
 */
public void removeCheckStateListener(ICheckStateListener listener);
/**
 * Sets the checked state for the given element in this viewer.
 *
 * @param element the element
 * @param state <code>true</code> if the item should be checked,
 *  and <code>false</code> if it should be unchecked
 * @return <code>true</code> if the checked state could be set, 
 *  and <code>false</code> otherwise
 */
public boolean setChecked(Object element, boolean state);
}
