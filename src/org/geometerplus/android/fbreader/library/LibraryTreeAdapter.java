/*
* FullReader+
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.library;

import org.geometerplus.android.fbreader.covers.CoverManager;
import org.geometerplus.android.fbreader.tree.TreeAdapter;
import org.geometerplus.fbreader.library.AuthorListTree;
import org.geometerplus.fbreader.library.AuthorTree;
import org.geometerplus.fbreader.library.FavoritesTree;
import org.geometerplus.fbreader.library.FileFirstLevelTree;
import org.geometerplus.fbreader.library.FileTree;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.fbreader.library.RecentBooksTree;
import org.geometerplus.fbreader.library.SearchResultsTree;
import org.geometerplus.fbreader.library.TagListTree;
import org.geometerplus.fbreader.library.TagTree;
import org.geometerplus.fbreader.library.TitleListTree;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.fullreader.R;

class LibraryTreeAdapter extends TreeAdapter {
	private CoverManager myCoverManager;
	private int res;
	private int COVER_HEIGHT;
	private int COVER_WIDTH;

	LibraryTreeAdapter(LibraryActivity activity, int res) {
		super(activity);
		this.res = res;

		Drawable folderIcon = activity.getResources().getDrawable(R.drawable.ic_list_library_book);
		COVER_HEIGHT = folderIcon.getIntrinsicHeight();
		COVER_WIDTH = folderIcon.getIntrinsicHeight();
	}
	
	@Override
	public int getCount() {
		if(super.getCount()==0)
			return 3;
		return super.getCount();
	}

	public View getView(int position, View convertView, final ViewGroup parent) {
		ViewHolder holder;
		View view = convertView;
		if (view == null) {
			view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
			holder = new ViewHolder();
			holder.title = ((TextView)view.findViewById(R.id.tw_library_title));
			holder.annot = ((TextView)view.findViewById(R.id.tw_library_annot));
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		FrameLayout fLayout = (FrameLayout) view.findViewById(R.id.shelf_layout);
		fLayout.setVisibility(View.VISIBLE);
		for (int i=0; i<fLayout.getChildCount(); i++){
			View view_1 = fLayout.getChildAt(i);
			view_1.setVisibility(View.VISIBLE);
		}
		if(position<super.getCount()){
			final LibraryTree tree = (LibraryTree)getItem(position);

			holder.title.setText(tree.getName());
			holder.annot.setText(tree.getSummary());
			FileTree fTree;
			// Проверка для файлов и папок Dropbox 
			// скачаны ли они на карту памяти или нет
			if (tree.getClass().getCanonicalName().equals(FileTree.class.getCanonicalName())){
				fTree = (FileTree) tree;
				// Скрываем документы в zip-архиве
				if (fTree.Parent.equals(FileTree.class)){
					FileTree parentTree = (FileTree) fTree.Parent;
					if (parentTree.getFile().isArchive()){
						if (fTree.getFile().getExtension().contains("pdf") 
							|| fTree.getFile().getExtension().contains("djvu")
							|| fTree.getFile().getExtension().contains("cbz")
							|| fTree.getFile().getExtension().contains("cbr")
							|| fTree.getFile().getExtension().contains("odt")
							|| fTree.getFile().getExtension().contains("rar")
							|| fTree.getFile().getExtension().contains("zip")
							|| fTree.getFile().getExtension().contains("xps")){
							fLayout.setVisibility(View.GONE);
							for (int i=0; i<fLayout.getChildCount(); i++){
								View view_1 = fLayout.getChildAt(i);
								view_1.setVisibility(View.GONE);
							}
						}
					}
				}
			}

			final ImageView coverView = (ImageView)view.findViewById(R.id.img_library_icon);
			if (myCoverManager == null) {
				myCoverManager = new CoverManager(getActivity(), COVER_HEIGHT, COVER_WIDTH);
			}

			myCoverManager.setupCoverView(coverView);
			coverView.setImageResource(getCoverResourceId(tree));
			coverView.post(new Runnable() {
				@Override
				public void run() {
					if(!myCoverManager.trySetCoverImage(coverView, tree)){
						coverView.setImageResource(getCoverResourceId(tree));
					}
				}
			});
			coverView.setVisibility(View.VISIBLE);
			holder.title.setVisibility(View.VISIBLE);
			holder.annot.setVisibility(View.VISIBLE);
		} else {
			final ImageView coverView = (ImageView)view.findViewById(R.id.img_library_icon);
			if (myCoverManager == null) {
				myCoverManager = new CoverManager(getActivity(), COVER_HEIGHT, COVER_WIDTH);
			}

			myCoverManager.setupCoverView(coverView);
			coverView.setVisibility(View.INVISIBLE);
			holder.title.setVisibility(View.INVISIBLE);
			holder.annot.setVisibility(View.INVISIBLE);
		}
		return view;
	}

	private int getCoverResourceId(LibraryTree tree) {
		if (tree.getBook() != null) {
			try{
				final ZLFile file = ((FileTree)tree).getFile();
				if(file.getExtension().equals("fb2")) {
					return R.drawable.fb2;
				}else if(file.getExtension().equals("doc")) {
					return R.drawable.doc;
				}else if(file.getExtension().equals("html")) {
					return R.drawable.html;
				}else if(file.getExtension().equals("mobi")) {
					return R.drawable.mobi;
				}else if(file.getExtension().equals("txt")) {
					return R.drawable.txt;
				}else if(file.getExtension().equals("rtf")) {
					return R.drawable.rtf;
				}else if(file.getExtension().equals("epub")) {
					return R.drawable.epub;
				}
			} catch(ClassCastException e) {
				
			}
		} else if (tree instanceof FavoritesTree) {
			return R.drawable.ic_list_library_favorites;
		} else if (tree instanceof RecentBooksTree) {
			return R.drawable.ic_list_library_recent;
		} else if (tree instanceof AuthorListTree) {
			return R.drawable.ic_list_library_authors;
		} else if (tree instanceof TitleListTree) {
			return R.drawable.ic_list_library_books;
		} else if (tree instanceof TagListTree) {
			return R.drawable.ic_list_library_tags;
		} else if (tree instanceof FileFirstLevelTree) {
			return R.drawable.ic_list_library_folder;
		} else if (tree instanceof SearchResultsTree) {
			return R.drawable.ic_list_library_search;
		} else if (tree instanceof FileTree) {
			final ZLFile file = ((FileTree)tree).getFile();
			
			if (file.isArchive()) {
				return R.drawable.ic_list_library_zip;
			} else if (file.isDirectory() && file.isReadable()) {
				return R.drawable.ic_list_library_folder;
			}else if(file.getExtension().equals("pdf")){
				return R.drawable.ic_list_library_book_pdf;
			}else if(file.getExtension().equals("djvu")){
				return R.drawable.ic_list_library_book_djvu;
			}else if(file.getExtension().equals("cbz")){
				return R.drawable.ic_list_library_book_cbz;
			}else if(file.getExtension().equals("cbr")){
				return R.drawable.ic_list_library_book_cbr;
			}else if(file.getExtension().equals("rar")){
				return R.drawable.ic_list_library_book_rar;
			}else if(file.getExtension().equals("odt")){
				return R.drawable.ic_list_library_book_odt;
			}else if(file.getExtension().equals("docx")){
				return R.drawable.ic_list_library_book_doxc;
			}else if(file.getExtension().equals("xps")){
				return R.drawable.ic_list_library_book_xps;
			}
			if(file.getExtension().equals("fb2")) {
				return R.drawable.fb2;
			}else if(file.getExtension().equals("doc")) {
				return R.drawable.doc;
			}else if(file.getExtension().equals("html")) {
				return R.drawable.html;
			}else if(file.getExtension().equals("mobi")) {
				return R.drawable.mobi;
			}else if(file.getExtension().equals("txt")) {
				return R.drawable.txt;
			}else if(file.getExtension().equals("rtf")) {
				return R.drawable.rtf;
			}else if(file.getExtension().equals("epub")) {
				return R.drawable.epub;
			}
			else {
				return R.drawable.ic_list_library_permission_denied;
			}
		} else if (tree instanceof AuthorTree) {
			return R.drawable.ic_list_library_author;
		} else if (tree instanceof TagTree) {
			return R.drawable.ic_list_library_tag;
		}
		
		return R.drawable.ic_list_library_books;
	}

	private static class ViewHolder{
		TextView title;
		TextView annot;
	}
}
