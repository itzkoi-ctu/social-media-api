"use client";

import { Post } from "@/types/types";
import React, { useEffect, useState } from "react";
import { getPosts } from "./actions";
import PostElem from "./post";
import { useInView } from "react-intersection-observer";
import { useCallback } from "react";



function Posts() {
  const [posts, setPosts] = useState<Post[]>([]);
  const [page, setPage] = useState<number>(0);
  const [error, setError] = useState<string>("");
  const [hasMore, setHasMore] = useState<boolean>(true);
  const [isLoading, setIsLoading] = useState(false);
  const [scrollTrigger, isInView] = useInView();

  const fetchPosts = useCallback(
    async (page: number) => {
      setIsLoading(true);
      try {
        const response = await getPosts(page);
        setHasMore(response.totalPages > page + 1);
        setPosts((prevPosts) =>
          page === 0 ? response.content : [...prevPosts, ...response.content]
        );
        setPage(page);
      } catch {
        setError("Failed to fetch posts");
      } finally {
        setIsLoading(false);
      }
    },
    [setHasMore, setPosts, setPage, setError]
  );

  useEffect(() => {
    fetchPosts(0);
  }, [fetchPosts]);

  useEffect(() => {
    if (isLoading || !hasMore || !isInView) {
      return;
    }
    fetchPosts(page + 1);
  }, [isInView, isLoading, hasMore, fetchPosts, page]);

  return (
    <div className="flex flex-col items-center justify-center gap-4">
      {error && <p className="text-red-500">{error}</p>}
      {posts.map((post) => (
        <PostElem post={post} key={post.id} />
      ))}
      {(!error && hasMore && !isLoading && (
        <div ref={scrollTrigger}>Loading...</div>
      )) || <p className="...">No more posts to load</p>}
    </div>
  );
}

export default Posts;
