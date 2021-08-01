const fs = require('fs');

fs.copyFile('./docs/api/dist/index.html', 'index.html', (err) => {
  if (err) {
    console.log(err);
  }
});