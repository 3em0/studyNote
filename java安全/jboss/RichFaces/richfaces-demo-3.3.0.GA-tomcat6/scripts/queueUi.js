var disabledImage = 'queue/examples/resources/button3.gif';
var enabledImage = 'queue/examples/resources/button2.gif';
var lastButtonActivated;
var lastButtonCounter;

function buttonpush(buttonName) {
    var button = document.getElementById(buttonName);
    var txt = document.createTextNode(buttonName); 
    if (button.value=='submit') {
        button.src = disabledImage;
        button.value = 'progress';
    }
 	if ((typeof lastButtonActivated=='undefined')||(lastButtonActivated.nodeValue != txt.nodeValue)){
	    lastButtonActivated=txt;
	    lastButtonCounter=1;
	    addCell(txt);
 	}
 	else{
    	changeCounterInCell(txt,++lastButtonCounter);
    }
} 
 
function buttonpop(buttonName) {
    var txt = document.createTextNode(buttonName);
    removeCell(txt);
} 

function addCell(cellData) {
    var cell = document.getElementById("tr1").insertCell(0);
    cell.setAttribute("height", "50px");
    cell.setAttribute("width", "50px"); 
    cell.innerHTML = cellData.nodeValue + '(1)';
//    cell.className = "queueCell";
}
function changeCounterInCell(cellData, counter){
    var row = document.getElementById("tr1");
    var cells = row.getElementsByTagName("td");
	cells[0].innerHTML=cellData.nodeValue + '(' + counter + ')';
}

function removeCell(cellData) {
    var row = document.getElementById("tr1");
    var cells = row.getElementsByTagName("td");
    var deletedCellIndex=-1;
    if (cells.length==1) {
		lastButtonActivated=undefined;
		lastButtonCounter=undefined;
    }
    if (typeof cells != 'undefined' || cells != null) {
        for (var i=cells.length-1; i>=0; i--) {
            if (cells[i].firstChild.nodeValue.indexOf(cellData.nodeValue.concat('('))==0){
                if (deletedCellIndex==-1){
                	deletedCellIndex=i;
	                row.deleteCell(i);
                }else{
                	deletedCellIndex=-1;
                	break;
                }
            }
        }
        if (deletedCellIndex!=-1){
                var button = document.getElementById(cellData.nodeValue);
                button.value = 'submit';
                button.src = enabledImage;
        }
    }
}

function errorMsg(eventName, data) {
    alert("Name: "+eventName+" Error Status: "+data.statusMessage);
}

// Listen for all queue events
//OpenAjax.hub.subscribe("javax.faces.Event.**",msg);
// Listen for all error events
//OpenAjax.hub.subscribe("javax.faces.Error.**",errorMsg);