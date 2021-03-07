function httpGet(url) {
    var xmlHttpRequest = new XMLHttpRequest();
    xmlHttpRequest.onreadystatechange = function() {
        if (xmlHttpRequest.readyState == 4 && xmlHttpRequest.status == 200) {
            console.log(xmlHttpRequest.responseText)
            var s = xmlHttpRequest.responseText;
            console.log(s);
            return s;
        }
    }
    xmlHttpRequest.open("GET", url, true);
    xmlHttpRequest.send(null);
}

var response = httpGet("https://status.mojang.com");

console.log(response);