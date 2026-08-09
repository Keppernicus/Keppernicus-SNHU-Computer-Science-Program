const mongoose = require('mongoose');
const readLine = require('readline');

// host is allowed to be overridden by an env variable, code works local and deployed
const host = process.env.DB_HOST || '127.0.0.1';
const dbURI = `mongodb://${host}/travlr`;


const connect = () => {
  setTimeout(() => mongoose.connect(dbURI), 1000);
};


mongoose.connection.on('connected', () => {
  console.log(`Mongoose connected to ${dbURI}`);
});

mongoose.connection.on('error', (err) => {
  console.log(`Mongoose connection error: ${err}`);
});

mongoose.connection.on('disconnected', () => {
  console.log('Mongoose disconnected');
});

// shutdown 
const gracefulShutdown = async (msg) => {
  await mongoose.connection.close();
  console.log(`Mongoose disconnected through ${msg}`);
};

// readline is to account for both windows and unix
if (process.platform === 'win32') {
  const rl = readLine.createInterface({
    input: process.stdin,
    output: process.stdout
  });
  rl.on('SIGINT', () => {
    process.emit('SIGINT');
  });
}

// nodemon restart
process.once('SIGUSR2', async () => {
  await gracefulShutdown('nodemon restart');
  process.kill(process.pid, 'SIGUSR2');
});

// app termination
process.on('SIGINT', async () => {
  await gracefulShutdown('app termination');
  process.exit(0);
});

// container termination
process.on('SIGTERM', async () => {
  await gracefulShutdown('app shutdown');
  process.exit(0);
});

connect();


require('./travlr');

module.exports = mongoose;
