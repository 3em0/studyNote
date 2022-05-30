package org.richfaces.demo.dnd;


	import org.richfaces.component.Dropzone;
import org.richfaces.event.DropEvent;
import org.richfaces.event.DropListener;
	
	
	public class EventBean implements DropListener {
		private org.richfaces.demo.dnd.DndBean dndBean;

		public void processDrop(DropEvent dropEvent) {
			Dropzone dropzone = (Dropzone) dropEvent.getComponent();
			dndBean.moveFramework(dropEvent.getDragValue(), dropzone.getDropValue());
		}

		public org.richfaces.demo.dnd.DndBean getDndBean() {
		return dndBean;
	}

		public void setDndBean(org.richfaces.demo.dnd.DndBean dndBean) {
		this.dndBean = dndBean;
	}
	}