window.addEventListener('load', (event) => {
    (({src}) => {
        const frameId = 'jahiaPagePreview4Semj'
        if(!document.getElementById(frameId)) {
            fetch(src)
                .then(response => {
                    // Check if the request was successful (status code 200-299)
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    // Parse the JSON in the response
                    return response.text();
                })
                .then(data => {
                    // const parser = new DOMParser()
                    // const doc = parser.parseFromString(data, 'text/html');
                    // const scriptTags = doc.getElementsByTagName('script');
                    // for (let scriptTag of scriptTags) {
                    //     scriptTag.parentNode.removeChild(scriptTag);
                    // }
                    //
                    // // Convert the modified document back to an HTML string
                    // const modifiedHtmlString = new XMLSerializer().serializeToString(doc);

                    // Do something with the data
                    // console.log('Data received:', data);
                    const iframe = document.createElement('iframe');
                    iframe.src = "data:text/html;charset=utf-8," + encodeURIComponent(data)
                    iframe.id = frameId;
                    // iframe.sandbox = "allow-forms";
                    document.body.appendChild(iframe)
                })
                .catch(error => {
                    // Handle errors
                    console.error('Error:', error);
                });
        }
        // const topWindow = window.top;
        // const topDocument = topWindow.document;
        // const frameId = 'jahiaPagePreview4Semj'
        // if(!topDocument.getElementById(frameId)){
        //     const iframe = topDocument.createElement('iframe');
        //
        //     // Set attributes for the iframe
        //     iframe.src = 'http://localhost:8080/fr/sites/industrial/home.html'//src; // Set the source URL
        //     // iframe.width = '0'; // Set the width
        //     // iframe.height = '0'; // Set the height
        //     iframe.id = frameId; // Set an ID for reference
        //     // iframe.style.border= '0';
        //     // iframe.style.visibility= 'hidden';
        //     // iframe.onload= () =>{
        //     //     const iframeContent = this.contentDocument || this.contentWindow?.document;
        //     //     console.log("iframeContent",iframeContent.body.innerHTML);
        //     // }
        //     // Append the iframe to the body or any other element
        //     topDocument.body.appendChild(iframe);
        // }
    })(window.semj4)
})