window.addEventListener('load', (event) => {
    (({src,isModuleEnabled}) => {
        const topWindow = window.top;
        const topDocument = topWindow.document;
        const frameId = 'jahiaPagePreview4Semj';
        const iframe = topDocument.getElementById(frameId)

        if(!isModuleEnabled && iframe){
            iframe.parentNode.removeChild(iframe);
        }

        if(isModuleEnabled && !iframe){
            const iframe = topDocument.createElement('iframe');

            // Set attributes for the iframe
            iframe.src = src; // Set the source URL
            iframe.id = frameId; // Set an ID for reference
            iframe.style.display= 'none';

            // Append the iframe to the body or any other element
            topDocument.body.appendChild(iframe);
        }
    })(window.semj4)
})