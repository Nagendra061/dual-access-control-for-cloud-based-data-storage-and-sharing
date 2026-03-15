// API Base URL
const API_URL = 'http://localhost:8080/api';

// Utility: Show Toast Notification
function showToast(message, type = 'success') {
    const toastContainer = document.getElementById('toast-container');
    if (!toastContainer) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';
    
    toast.innerHTML = `
        <i class="fas ${icon} toast-icon"></i>
        <span>${message}</span>
    `;
    
    toastContainer.appendChild(toast);
    
    // Trigger animation
    setTimeout(() => toast.classList.add('show'), 10);
    
    // Remove toast
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Global Headers Setup
function getAuthHeaders() {
    const token = localStorage.getItem('dac_token');
    return {
        'Authorization': `Bearer ${token}`
    };
}

// Generic Fetch Wrapper
async function apiCall(endpoint, options = {}) {
    const url = `${API_URL}${endpoint}`;
    try {
        const response = await fetch(url, options);
        let data;
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.indexOf("application/json") !== -1) {
            data = await response.json();
        } else {
            data = await response.text();
        }
        
        if (!response.ok) {
            throw new Error(data.message || data || 'API request failed');
        }
        return data;
    } catch (error) {
        showToast(error.message, 'error');
        console.error('API Error:', error);
        throw error;
    }
}

// ----------------------------------------------------------------------------
// Authentication Logic (index.html)
// ----------------------------------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
    // Check if on Auth page
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    
    if (loginForm && registerForm) {
        // Toggle forms
        document.getElementById('showRegister').addEventListener('click', (e) => {
            e.preventDefault();
            loginForm.classList.add('hidden');
            registerForm.classList.remove('hidden');
        });

        document.getElementById('showLogin').addEventListener('click', (e) => {
            e.preventDefault();
            registerForm.classList.add('hidden');
            loginForm.classList.remove('hidden');
        });

        // Toggle Password Visibility
        document.querySelectorAll('.toggle-password').forEach(icon => {
            icon.addEventListener('click', function() {
                const inputId = this.getAttribute('id') === 'toggleLoginPassword' ? 'loginPassword' : 'regPassword';
                const input = document.getElementById(inputId);
                
                if (input.type === 'password') {
                    input.type = 'text';
                    this.classList.remove('fa-eye');
                    this.classList.add('fa-eye-slash');
                } else {
                    input.type = 'password';
                    this.classList.remove('fa-eye-slash');
                    this.classList.add('fa-eye');
                }
            });
        });

        // Handle Login
        document.getElementById('loginFormElement').addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('loginUsername').value;
            const password = document.getElementById('loginPassword').value;
            const btn = document.getElementById('loginBtn');
            
            try {
                btn.innerHTML = '<span class="loader"></span>';
                btn.disabled = true;

                const data = await apiCall('/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password })
                });

                showToast('Login successful!');
                localStorage.setItem('dac_token', data.token);
                localStorage.setItem('dac_user', JSON.stringify({
                    username: data.username,
                    email: data.email,
                    fullName: data.fullName,
                    roles: data.roles
                }));
                
                setTimeout(() => {
                    window.location.href = '/dashboard.html';
                }, 1000);

            } catch (error) {
                btn.innerHTML = '<span>Sign In</span>';
                btn.disabled = false;
            }
        });

        // Handle Register
        document.getElementById('registerFormElement').addEventListener('submit', async (e) => {
            e.preventDefault();
            const fullName = document.getElementById('regFullName').value;
            const email = document.getElementById('regEmail').value;
            const username = document.getElementById('regUsername').value;
            const password = document.getElementById('regPassword').value;
            const btn = document.getElementById('registerBtn');

            try {
                btn.innerHTML = '<span class="loader"></span>';
                btn.disabled = true;

                await apiCall('/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ fullName, email, username, password })
                });

                showToast('Registration successful! Please login.');
                document.getElementById('showLogin').click();
                
                btn.innerHTML = '<span>Create Account</span>';
                btn.disabled = false;

            } catch (error) {
                btn.innerHTML = '<span>Create Account</span>';
                btn.disabled = false;
            }
        });
    }
});

// ----------------------------------------------------------------------------
// Dashboard Logic (dashboard.html)
// ----------------------------------------------------------------------------
window.isAppInitialized = true; // prevent inline script conflicts

document.addEventListener('DOMContentLoaded', () => {
    // Check if on Dashboard
    if (document.getElementById('userProfile') || document.getElementById('logoutBtn')) {
        
        // Validate session
        const token = localStorage.getItem('dac_token');
        const userStr = localStorage.getItem('dac_user');
        
        if (!token || !userStr) {
            window.location.href = '/';
            return;
        }

        const user = JSON.parse(userStr);
        
        // Populate User Info
        document.getElementById('userNameDisplay').textContent = user.fullName || user.username;
        document.getElementById('userRoleDisplay').textContent = user.roles ? user.roles.join(', ') : 'User';
        document.getElementById('userInitial').textContent = (user.fullName || user.username).charAt(0).toUpperCase();

        // Handle Logout
        document.getElementById('logoutBtn').addEventListener('click', () => {
            localStorage.removeItem('dac_token');
            localStorage.removeItem('dac_user');
            window.location.href = '/';
        });

        // Tab Navigation
        const navLinks = document.querySelectorAll('.nav-link');
        navLinks.forEach(link => {
            link.addEventListener('click', () => {
                const tab = link.getAttribute('data-tab');
                switchTab(tab);
            });
        });

        // File Upload Logic
        const dropZone = document.getElementById('dropZone');
        const fileInput = document.getElementById('fileInput');
        const selectedFileName = document.getElementById('selectedFileName');
        const uploadForm = document.getElementById('uploadForm');

        if(dropZone) {
            dropZone.addEventListener('click', () => fileInput.click());

            dropZone.addEventListener('dragover', (e) => {
                e.preventDefault();
                dropZone.classList.add('dragover');
            });

            dropZone.addEventListener('dragleave', () => dropZone.classList.remove('dragover'));

            dropZone.addEventListener('drop', (e) => {
                e.preventDefault();
                dropZone.classList.remove('dragover');
                if (e.dataTransfer.files.length) {
                    fileInput.files = e.dataTransfer.files;
                    selectedFileName.textContent = `Selected: ${fileInput.files[0].name}`;
                }
            });

            fileInput.addEventListener('change', () => {
                if (fileInput.files.length) {
                    selectedFileName.textContent = `Selected: ${fileInput.files[0].name}`;
                }
            });

            uploadForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                
                if (!fileInput.files.length) {
                    showToast('Please select a file first', 'error');
                    return;
                }

                const file = fileInput.files[0];
                const description = document.getElementById('fileDescription').value;
                const btn = document.getElementById('uploadBtnSubmit');

                const formData = new FormData();
                formData.append('file', file);
                formData.append('description', description);

                try {
                    btn.innerHTML = '<span class="loader"></span> Encrypting...';
                    btn.disabled = true;

                    await fetch(`${API_URL}/files/upload`, {
                        method: 'POST',
                        headers: getAuthHeaders(), // No Content-Type, browser sets multipart
                        body: formData
                    }).then(async (res) => {
                        const data = await res.json();
                        if (!res.ok) throw new Error(data.message || 'Upload failed');
                        return data;
                    });

                    showToast('File encrypted and uploaded securely!');
                    
                    // Reset form and go back to files
                    uploadForm.reset();
                    selectedFileName.textContent = '';
                    switchTab('my-files');

                } catch (error) {
                    showToast(error.message, 'error');
                    console.error('Upload Error:', error);
                } finally {
                    btn.innerHTML = '<span><i class="fas fa-lock"></i> Encrypt & Upload</span>';
                    btn.disabled = false;
                }
            });
        }

        // Initialize view
        loadFiles('my-files');
    }
});

// UI View Switcher
function switchTab(tabId) {
    const listSection = document.getElementById('filesListSection');
    const uploadSection = document.getElementById('uploadSection');
    const pageTitle = document.getElementById('pageTitle');
    const topUploadBtn = document.getElementById('topUploadBtn');
    
    // Update active nav
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    
    const activeLink = document.querySelector(`.nav-link[data-tab="${tabId}"]`);
    if(activeLink) activeLink.classList.add('active');

    if (tabId === 'upload') {
        listSection.classList.add('hidden');
        uploadSection.classList.remove('hidden');
        pageTitle.textContent = 'Upload Secure File';
        topUploadBtn.classList.add('hidden');
    } else {
        uploadSection.classList.add('hidden');
        listSection.classList.remove('hidden');
        topUploadBtn.classList.remove('hidden');
        
        if (tabId === 'my-files') {
            pageTitle.textContent = 'My Secure Files';
            loadFiles('my-files');
        } else if (tabId === 'all-files') {
            pageTitle.textContent = 'Enterprise Files (All)';
            loadFiles('all');
        }
    }
}

// Fetch and Render Files
async function loadFiles(type) {
    const tableBody = document.getElementById('filesTableBody');
    if (!tableBody) return;

    tableBody.innerHTML = '<tr><td colspan="5" class="text-center"><span class="loader"></span> Loading securely...</td></tr>';
    
    try {
        const endpoint = type === 'my-files' ? '/files/my-files' : '/files/all';
        const files = await apiCall(endpoint, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        renderFilesTable(files, type);

    } catch (error) {
        tableBody.innerHTML = `<tr><td colspan="5" class="text-center text-danger">Failed to load files: ${error.message}</td></tr>`;
    }
}

function renderFilesTable(files, type) {
    const tableBody = document.getElementById('filesTableBody');
    tableBody.innerHTML = '';

    if (!files || files.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="5" class="text-center">No files found.</td></tr>';
        return;
    }

    const { username } = JSON.parse(localStorage.getItem('dac_user'));

    files.forEach(file => {
        const tr = document.createElement('tr');
        
        // Ownership Badge
        let badge = '';
        if (file.ownerUsername === username) {
            badge = '<span class="badge badge-owner">Owner</span>';
        } else {
            badge = '<span class="badge badge-shared">Shared</span>';
        }
        
        // Date formatting
        const uploadDate = new Date(file.uploadDate).toLocaleDateString('en-US', {
            year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
        });

        const iconClass = file.fileName.endsWith('.pdf') ? 'fa-file-pdf' : 
                         file.fileName.endsWith('.txt') ? 'fa-file-alt' : 
                         'fa-file';

        tr.innerHTML = `
            <td>
                <div class="file-name">
                    <i class="fas ${iconClass}"></i>
                    ${file.fileName}
                </div>
            </td>
            <td>${file.description || 'No description'}</td>
            <td>
                ${file.ownerUsername} ${badge}
            </td>
            <td>${uploadDate}</td>
            <td class="actions">
                <button class="btn-action download" onclick="downloadFile(${file.id}, '${file.fileName}')" title="Decrypt & Download">
                    <i class="fas fa-download"></i>
                </button>
                ${file.ownerUsername === username ? `
                <button class="btn-action delete" onclick="deleteFile(${file.id})" title="Delete File">
                    <i class="fas fa-trash-alt"></i>
                </button>
                ` : ''}
            </td>
        `;
        tableBody.appendChild(tr);
    });
}

// Download File
async function downloadFile(fileId, fileName) {
    try {
        showToast('Decrypting and preparing download...');
        
        const response = await fetch(`${API_URL}/files/${fileId}/download`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.message || 'Access Denied or Download failed');
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        
        showToast('Download complete!');

    } catch (error) {
        showToast(error.message, 'error');
        console.error('Download error:', error);
    }
}

// Delete File
async function deleteFile(fileId) {
    if (!confirm('Are you sure you want to delete this file permanently?')) return;

    try {
        await apiCall(`/files/${fileId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        
        showToast('File deleted successfully');
        
        // Reload current view
        const activeTab = document.querySelector('.nav-link.active');
        if (activeTab) {
            switchTab(activeTab.getAttribute('data-tab'));
        } else {
            loadFiles('my-files');
        }
        
    } catch (error) {
        console.error('Delete error:', error);
    }
}
