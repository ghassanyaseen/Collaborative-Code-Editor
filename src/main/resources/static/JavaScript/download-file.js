// Download file function
function downloadFile(fileName) {
    fetch(`/api/files/download?filename=${encodeURIComponent(fileName)}`)
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    console.error('Download failed:', text);
                    throw new Error('Network response was not ok: ' + response.status);
                });
            }
            return response.blob(); // Get the file as a Blob
        })
        .then(blob => {
            const url = window.URL.createObjectURL(blob); // Create a URL for the Blob
            const a = document.createElement('a'); // Create an anchor element
            a.style.display = 'none'; // Hide the anchor
            a.href = url; // Set the URL to the Blob
            a.download = fileName; // Set the download attribute with the file name
            document.body.appendChild(a); // Append the anchor to the body
            a.click(); // Programmatically click the anchor to trigger the download
            window.URL.revokeObjectURL(url); // Clean up the URL object
            document.body.removeChild(a); // Remove the anchor from the document
        })
        .catch(error => {
            console.error('There was an error downloading the file:', error);
            alert('Error downloading file: ' + error.message);
        });
}
