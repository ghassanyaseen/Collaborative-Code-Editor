document.addEventListener("DOMContentLoaded", function() {
    const fileId = window.location.pathname.split("/").pop(); // Get the last part of the URL

    // Fetch file details
    fetch(`/api/files/${fileId}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById("fileNameDisplay").textContent = data.fileName; // Update to use textContent for display
            document.getElementById("content").value = data.content; // Ensure the textarea is set correctly
        })
        .catch(error => console.error('Error fetching file:', error));

    // Fetch version history
    fetch(`/api/files/${fileId}/versions`)
        .then(response => response.json())
        .then(versions => {
            const versionList = document.getElementById('versionList');
            versions.forEach(version => {
                const listItem = document.createElement('li');
                listItem.innerHTML = `
                    Version ${version.versionNumber} - Updated at ${version.timestamp}
                    <button onclick="viewVersion(${version.id})">View Details</button>
                `;
                versionList.appendChild(listItem);
            });
        })
        .catch(error => console.error('Error fetching versions:', error));

    // Fetch comments
    fetch(`/api/files/${fileId}/comments`)
        .then(response => response.json())
        .then(comments => {
            const commentList = document.getElementById('commentList');
            comments.forEach(comment => {
                const listItem = document.createElement('li');
                listItem.textContent = `Line ${comment.lineNumber}: ${comment.content} (by ${comment.createdBy})`;
                commentList.appendChild(listItem);
            });
        })
        .catch(error => console.error('Error fetching comments:', error));

    // Add comment functionality
    const addCommentButton = document.getElementById("addCommentButton");
    if (addCommentButton) {
        addCommentButton.onclick = function() {
            const lineNumber = document.getElementById("lineNumber").value;
            const commentContent = document.getElementById("commentContent").value;

            fetch(`/api/files/${fileId}/comments`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({ lineNumber: lineNumber, content: commentContent })
            })
                .then(response => {
                    if (response.ok) {
                        alert('Comment added successfully!');
                        window.location.reload();
                    } else {
                        return response.text().then(text => {
                            alert('Error adding comment: ' + text);
                        });
                    }
                })
                .catch(error => console.error('Error adding comment:', error));
        };
    }

    // View a specific version's details
    window.viewVersion = function(versionId) {
        fetch(`/api/files/version/${versionId}`)
            .then(response => response.json())
            .then(version => {
                alert(`Version ID: ${version.id}\nContent: ${version.content}`);
            })
            .catch(error => console.error('Error fetching version details:', error));
    };
});
