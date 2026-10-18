
const pool = require("../db/pool");

async function getProducts(req, res) {
  try {
    const result = await pool.query(`
      SELECT id, seller_type, brand, category, product_type,
             title, description, price, location, images, created_at
      FROM products
      WHERE approval_status = 'approved'
      ORDER BY created_at DESC
    `);

    res.json({ products: result.rows });
  } catch (error) {
    console.error("Get products error:", error.message);
    res.status(500).json({ message: "Could not fetch products" });
  }
}

async function createProduct(req, res) {
  try {
    const {
      seller_type, brand, category, product_type,
      title, description, price, location, images
    } = req.body;

    if (!["individual", "vendor"].includes(seller_type)) {
      return res.status(400).json({
        message: "Seller type must be individual or vendor",
      });
    }

    if (
      !category?.trim() ||
      !title?.trim() ||
      !location?.trim() ||
      price === undefined ||
      price === null ||
      price === "" ||
      !Number.isFinite(Number(price)) ||
      Number(price) < 0
    ) {
      return res.status(400).json({
        message: "Valid category, title, location and price are required",
      });
    }

    if (images !== undefined &&
        (!Array.isArray(images) ||
         !images.every(image => typeof image === "string"))) {
      return res.status(400).json({
        message: "Images must be an array of strings",
      });
    }

    const result = await pool.query(
      `INSERT INTO products
       (seller_id, seller_type, brand, category, product_type,
        title, description, price, location, images)
       VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10)
       RETURNING id, seller_type, brand, category, product_type,
                 title, description, price, location, images,
                 approval_status, created_at`,
      [
        req.user.id,
        seller_type,
        brand?.trim() || null,
        category.trim(),
        product_type?.trim() || null,
        title.trim(),
        description?.trim() || null,
        Number(price),
        location.trim(),
        JSON.stringify(images || []),
      ]
    );

    res.status(201).json({
      message: "Listing submitted for approval",
      product: result.rows[0],
    });
  } catch (error) {
    console.error("Create product error:", error.message);
    res.status(500).json({ message: "Could not create listing" });
  }
}

module.exports = { getProducts, createProduct };
