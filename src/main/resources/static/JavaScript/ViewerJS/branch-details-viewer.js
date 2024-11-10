const branchId = window.location.pathname.split("/").pop();  // Get the branch ID from the URL

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
        folderList.innerHTML = '<tr><td colspan="1">No folders found</td></tr>';
    } else {
        rootFolders.forEach(folder => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>
                    <button class="folder-button" onclick="location.href='/viewer/folder-viewer/${folder.id}'">${folder.folderName}</button>
                </td>
            `;
            folderList.appendChild(row);
        });
    }
}

// Load files
function loadFiles(files) {
    const fileList = document.getElementById('fileList').getElementsByTagName('tbody')[0];
    fileList.innerHTML = '';

    const filteredFiles = files.filter(file => file.parentFolderId === null);

    if (filteredFiles.length === 0) {
        fileList.innerHTML = '<tr><td colspan="3">No files found</td></tr>';
    } else {
        filteredFiles.forEach(file => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>
                    <button class="file-button" onclick="location.href='/viewer/file-viewer/${file.id}'">${file.fileName}</button>
                </td>
                <td>${file.languageType}</td>
                <td>${file.createdBy || 'Unknown'}</td>
            `;
            fileList.appendChild(row);
        });
    }
}

// Load branch details on page load
document.addEventListener('DOMContentLoaded', loadBranchDetails);
