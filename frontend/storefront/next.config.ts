import type { NextConfig } from "next";
import path from "path";

const nextConfig: NextConfig = {
  reactCompiler: true,
  transpilePackages: [path.resolve(__dirname, "../shared")],
  turbopack: {
    root: path.resolve(__dirname, "../.."),
  },
};

export default nextConfig;
