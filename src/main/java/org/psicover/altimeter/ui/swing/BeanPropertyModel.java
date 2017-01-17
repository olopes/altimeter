package org.psicover.altimeter.ui.swing;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.psicover.altimeter.LogFactory;

public class BeanPropertyModel {
	private static final Logger logger = LogFactory.getLogger(BeanPropertyModel.class);
	
	private final Map<String, PropertyDescriptor> properties;
	private final Map<String, BeanListener> listeners;
	private final Object bean;
	
	public BeanPropertyModel(final Object bean) {
		this.bean=bean;
		this.properties = new HashMap<>();
		this.listeners = new HashMap<>();
		buildPropertiesMap();
	}
	
	private void buildPropertiesMap() {
		try {
			PropertyDescriptor[] descriptors = Introspector.getBeanInfo(this.bean.getClass()).getPropertyDescriptors();
		
			for(PropertyDescriptor o : descriptors)
				this.properties.put(o.getName(), o);
			
		} catch (Exception e) {
			logger.warning("Could not build properties map: "+e.getMessage()); //$NON-NLS-1$
		}
	}
	
	public void set(String property, Object value) {
		PropertyDescriptor p = this.properties.get(property);
		try {
			p.getWriteMethod().invoke(bean, value);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.warning("Could not set property '"+property+"' value: "+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public Object get(String property) {
		PropertyDescriptor p = this.properties.get(property);
		
		try {
			return p.getReadMethod().invoke(bean);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.warning("Could not get property '"+property+"' value: "+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}

	public BeanListener bindListener(final String property) {
		BeanListener listener = this.listeners.get(property);
		if(null == listener) {
			this.listeners.put(property,listener = new BeanListener(property));
		}
		
		return listener;
	}
	
	public class BeanListener implements PropertyChangeListener, ItemListener, DocumentListener {
		private String property;
		BeanListener(String property) {
			this.property=property;
		}
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			set(property, evt.getNewValue());
		}
	    @Override
	    public void itemStateChanged(ItemEvent event) {
	    	if (event.getStateChange() == ItemEvent.SELECTED) {
	    		Object item = event.getItem();
	    		set(property, item);
	    	}
	    }
	    
	    private String getText(final Document doc) {
	        String txt;
	        try {
	            txt = doc.getText(0, doc.getLength());
	        } catch (BadLocationException e) {
	            txt = null;
	        }
	        return txt;
	    }

	    @Override
	    public void changedUpdate(DocumentEvent evt) {
			set(property, getText(evt.getDocument()));
	    }
	    @Override
	    public void insertUpdate(DocumentEvent evt) {
			set(property, getText(evt.getDocument()));
	    }
	    @Override
	    public void removeUpdate(DocumentEvent evt) {
			set(property, getText(evt.getDocument()));
	    }
	}

}
