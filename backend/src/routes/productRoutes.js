
const express = require("express");
const authenticateToken = require("../middleware/authMiddleware");
const {
  getProducts,
  createProduct,
} = require("../controllers/productController");

const router = express.Router();

router.get("/", getProducts);
router.post("/", authenticateToken, createProduct);

module.exports = router;
