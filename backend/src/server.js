const express = require("express");
const pool = require("./db/pool");
const productRoutes = require("./routes/productRoutes");
const authRoutes = require("./routes/authRoutes");

const app = express();

app.use(express.json());


app.use("/api/auth", authRoutes);
app.use("/api/products", productRoutes);

app.get("/health", async (req, res) => {
  try {
    const result = await pool.query(
      "SELECT current_database() AS database, current_user AS user"
    );

    res.json({
      status: "ok",
      database: result.rows[0].database,
      user: result.rows[0].user,
    });
  } catch (error) {
    console.error("Database connection failed:", error.message);
    res.status(500).json({ status: "error", message: "Database unavailable" });
  }
});

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
  console.log(`Bid.ai API running on port ${PORT}`);
});