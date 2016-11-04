xmlhttp = new XMLHttpRequest();
var url = "/accept";
xmlhttp.open("POST", url, true);
xmlhttp.setRequestHeader("Content-type", "application/json");
xmlhttp.setRequestHeader("X-Parse-Application-Id", "VnxVYV8ndyp6hE7FlPxBdXdhxTCmxX1111111");
xmlhttp.setRequestHeader("X-Parse-REST-API-Key","6QzJ0FRSPIhXbEziFFPs7JvH1l11111111");
xmlhttp.onreadystatechange = function () { //Call a function when the state changes.
    if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
        alert(xmlhttp.responseText);
    }
}
//var parameters = {
//    "ephrase": "english",
//    "pphrase": "farsi",
//     "nvote": 0,
//    "yvote": 0
//};
// Neither was accepted when I set with parameters="username=myname"+"&password=mypass" as the server may not accept that

function doPost() {
  xmlhttp.send("{}}");
}