var nodemailer = require("nodemailer");
const transporter = nodemailer.createTransport({
    service: "gmail",
    auth: {
        user: "datntph51025@gmail.com",
        pass: "nenf mvun wfls kvtj"
    }
});
module.exports = transporter 