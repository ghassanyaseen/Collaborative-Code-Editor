// Create a new branch
document.getElementById('createBranchButton').onclick = function() {
    const branchName = document.getElementById('branchName').value;

    if (!branchName) {
        alert('Please enter a branch name.');
        return;
    }

    fetch('/api/branches/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({ branchName: branchName })
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return response.text().then(text => {
                    throw new Error(text);
                });
            }
        })
        .then(data => {
            loadBranches();
            document.getElementById('branchName').value = '';
        })
        .catch(error => {
            alert('Error creating branch: ' + error.message);
        });
};


// Merge two branches by name
document.getElementById('mergeBranchButton').onclick = function() {
    const targetBranchName = document.getElementById('targetBranchName').value;
    const sourceBranchName = document.getElementById('sourceBranchName').value;

    if (!targetBranchName || !sourceBranchName) {
        alert('Please enter both branch names.');
        return;
    }

    fetch('/api/branches/merge', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({ targetBranchName: targetBranchName, sourceBranchName: sourceBranchName })
    })
        .then(response => {
            if (response.ok) {
                loadBranches();
                alert('Branches merged successfully!');
            } else {
                return response.text().then(text => {
                    throw new Error(text);
                });
            }
        })
        .catch(error => {
            alert('Error merging branches: ' + error.message);
        });
};


// Clone a branch
document.getElementById('cloneBranchButton').onclick = function() {
    const newBranchName = document.getElementById('newBranchName').value;
    const sourceBranchName = document.getElementById('sourceBranchForClone').value;

    if (!newBranchName || !sourceBranchName) {
        alert('Please enter both branch names.');
        return;
    }

    fetch('/api/branches/clone', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({ newBranchName: newBranchName, sourceBranchName: sourceBranchName })
    })
        .then(response => {
            if (response.ok) {
                loadBranches();
                document.getElementById('newBranchName').value = '';
                document.getElementById('sourceBranchForClone').value = '';
            } else {
                return response.text().then(text => {
                    throw new Error(text);
                });
            }
        })
        .catch(error => {
            alert('Error cloning branch: ' + error.message);
        });
};


// Load branches
function loadBranches() {
    fetch('/api/branches')
        .then(response => response.json())
        .then(branches => {
            const branchList = document.getElementById('branchList');
            branchList.innerHTML = '';

            branches.forEach(branch => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>
                        <button class="branchButton" data-branch-id="${branch.id}">${branch.branchName}</button>
                    </td>
                    <td>Created by: ${branch.createdBy}</td>
                    <td>
                        <button class="deleteBranchButton" data-branch-id="${branch.id}" style="background-color: #e74c3c; color: white;">Delete</button>
                    </td>
                `;
                branchList.appendChild(row);
            });

            document.querySelectorAll('.branchButton').forEach(button => {
                button.onclick = function() {
                    const branchId = this.getAttribute('data-branch-id');
                    viewBranchDetails(branchId);
                };
            });

            document.querySelectorAll('.deleteBranchButton').forEach(button => {
                button.onclick = function() {
                    const branchId = this.getAttribute('data-branch-id');
                    deleteBranch(branchId); // Function to delete the branch
                };
            });
        });
}

// Function to view branch details
function viewBranchDetails(branchId) {
    window.location.href = `/editor/branch-detail-editor/${branchId}`; // Adjust this URL to match your routing structure
}

// Function to delete a branch
function deleteBranch(branchId) {
    if (!confirm('Are you sure you want to delete this branch?')) {
        return;
    }

    fetch(`/api/branches/${branchId}`, {
        method: 'DELETE',
    })
        .then(response => {
            if (response.ok) {
                alert('Branch deleted successfully!');
                loadBranches();
            } else {
                return response.text().then(text => {
                    throw new Error(text);
                });
            }
        })
        .catch(error => {
            alert('Error deleting branch: ' + error.message);
        });
}


// Load branches on page load
loadBranches();
