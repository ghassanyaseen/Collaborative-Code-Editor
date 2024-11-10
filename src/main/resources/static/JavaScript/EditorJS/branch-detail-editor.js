const branchId = window.location.pathname.split("/").pop(); // Get the branch ID from the URL

// Create new folder inside the current branch
document.getElementById('createFolderInsideButton').onclick = function () {
    const folderName = document.getElementById('newFolderName').value;
    const parentFolderId = 0;

    if (!folderName) {
        alert('Please enter a folder name.');
        return;
    }

    fetch(`/api/folders/create`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
            folderName: folderName,
            parentFolderId: parentFolderId,
            branchId: branchId
        })
    })
        .then(response => {
            if (response.ok) {
                return response.text().then(text => {
                    alert(text);
                    loadBranchDetails();
                });
            } else {
                return response.text().then(text => {
                    alert('Error creating folder: ' + text);
                });
            }
        })
        .catch(error => console.error('Error creating folder:', error));
};

// Create new file inside the current branch
document.getElementById('createFileInsideButton').onclick = function () {
    const fileName = document.getElementById('newFileName').value;
    const languageType = document.getElementById('languageType').value;

    if (!fileName) {
        alert('Please enter a file name.');
        return;
    }

    fetch(`/api/files/create`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
            fileName: fileName,
            content: '', // Empty content by default
            languageType: languageType,
            parentFolderId: 0,
            branchId: branchId
        })
    })
        .then(response => {
            if (response.ok) {
                alert('File created successfully!');
                loadBranchDetails();
            } else {
                return response.text().then(text => {
                    alert('Error creating file: ' + text);
                });
            }
        })
        .catch(error => console.error('Error creating file:', error));
};

// Download file
function downloadFile(fileName) {
    fetch(`/api/files/download?filename=${encodeURIComponent(fileName)}`)
        .then(response => {
            if (!response.ok) {
                // Log the response status and body
                return response.text().then(text => {
                    console.error('Download failed:', text);
                    throw new Error('Network response was not ok: ' + response.status);
                });
            }
            return response.blob();
        })
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        })
        .catch(error => {
            console.error('There was an error downloading the file:', error);
            alert('Error downloading file: ' + error.message);
        });
}

// Load branch details
function loadBranchDetails() {
    fetch(`/api/branches/${branchId}`)
        .then(response => response.json())
        .then(branch => {
            console.log(branch);
            document.getElementById('branchName').textContent = branch.branchName;
            document.getElementById('createdBy').textContent = branch.createdBy;

            // Load folders and files
            loadFolders(branch.subFolders || []);
            loadFiles(branch.subFiles || []);
        })
        .catch(error => console.error('Error fetching branch details:', error));
}

// Load folders
function loadFolders(folders) {
    const folderList = document.getElementById('folderList').getElementsByTagName('tbody')[0];
    folderList.innerHTML = '';

    const rootFolders = folders.filter(folder => folder.parentFolderId === null);

    if (rootFolders.length === 0) {
        folderList.innerHTML = '<tr><td colspan="2">No folders found</td></tr>';
    } else {
        rootFolders.forEach(folder => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>
                    <button class="folder-button" onclick="location.href='${window.location.origin}/editor/folder-editor/${folder.id}'">${folder.folderName}</button>
                </td>
                <td>
                    <button class="deleteFolderButton" data-folder-id="${folder.id}">Delete</button>
                </td>
            `;
            folderList.appendChild(row);
        });

        // Attach delete functionality to each button
        document.querySelectorAll('.deleteFolderButton').forEach(button => {
            button.onclick = function () {
                const folderId = this.getAttribute('data-folder-id');
                deleteFolder(folderId);
            };
        });
    }
}

// Delete folder
function deleteFolder(folderId) {
    if (!confirm('Are you sure you want to delete this folder and all its contents?')) {
        return;
    }

    fetch(`/api/folders/${folderId}`, {
        method: 'DELETE',
    })
        .then(response => {
            if (response.ok) {
                alert('Folder deleted successfully!');
                loadBranchDetails();
            } else {
                return response.text().then(text => {
                    alert('Error deleting folder: ' + text);
                });
            }
        })
        .catch(error => {
            console.error('Error deleting folder:', error);
            alert('An unexpected error occurred while deleting the folder: ' + error.message);
        });
}


// Load files
function loadFiles(files) {
    const fileList = document.getElementById('fileList').getElementsByTagName('tbody')[0];
    fileList.innerHTML = '';

    const filteredFiles = files.filter(file => file.parentFolderId === null);

    if (filteredFiles.length === 0) {
        fileList.innerHTML = '<tr><td colspan="4">No files found</td></tr>';
    } else {
        filteredFiles.forEach(file => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>
                    <button class="file-button" onclick="location.href='/editor/file-editor/${file.id}'">${file.fileName}</button>
                </td>
                <td>${file.languageType}</td>
                <td>${file.createdBy || 'Unknown'}</td>
                <td>
                    <button class="deleteFileButton" data-file-id="${file.id}">Delete</button>
                    <button class="downloadFileButton" onclick="downloadFile('${file.fileName}')">Download</button>
                </td>
            `;
            fileList.appendChild(row);
        });

        // Attach delete functionality to each button
        document.querySelectorAll('.deleteFileButton').forEach(button => {
            button.onclick = function () {
                const fileId = this.getAttribute('data-file-id');
                deleteFile(fileId);
            };
        });
    }
}

// Delete file function
function deleteFile(fileId) {
    if (!confirm('Are you sure you want to delete this file?')) {
        return;
    }

    fetch(`/api/files/delete/${fileId}`, {
        method: 'DELETE',
    })
        .then(response => {
            if (response.ok) {
                alert('File deleted successfully!');
                loadBranchDetails();
            } else {
                return response.text().then(text => {
                    alert('Error deleting file: ' + text);
                });
            }
        })
        .catch(error => {
            console.error('Error deleting file:', error);
            alert('An unexpected error occurred while deleting the file: ' + error.message);
        });
}

// Load branch details on page load
document.addEventListener('DOMContentLoaded', loadBranchDetails);
