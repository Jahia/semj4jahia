window.addEventListener('load', (event) => {
    (({src,isModuleEnabled,frameId}) => {
        const topDocument = window.top.document;
        const iframe = topDocument.getElementById(frameId)

        if(!isModuleEnabled && iframe){
            iframe.parentNode.removeChild(iframe);
        }

        if(isModuleEnabled){
            if( iframe && iframe.src !== src){
                iframe.src = src;
            }
            if(!iframe){
                const iframe = topDocument.createElement('iframe');

                // Set attributes for the iframe
                iframe.src = src; // Set the source URL
                iframe.id = frameId; // Set an ID for reference
                iframe.style.display= 'none';

                // Append the iframe to the body or any other element
                topDocument.body.appendChild(iframe);
            }
        }
    })(window.semj4)
})